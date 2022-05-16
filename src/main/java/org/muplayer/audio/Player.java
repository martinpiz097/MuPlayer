package org.muplayer.audio;

import lombok.extern.java.Log;
import org.muplayer.audio.trackstates.TrackState;
import org.muplayer.audio.trackstates.UnknownState;
import org.muplayer.exception.MuPlayerException;
import org.muplayer.info.*;
import org.muplayer.interfaces.PlayerControl;
import org.muplayer.interfaces.PlayerListener;
import org.muplayer.model.*;
import org.muplayer.thread.ListenerRunner;
import org.muplayer.thread.TaskRunner;
import org.muplayer.thread.ThreadUtil;
import org.muplayer.util.AudioUtil;
import org.muplayer.util.FileUtil;
import org.muplayer.util.IOUtil;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.LogManager;
import java.util.stream.Collectors;

import static org.muplayer.info.ListenersNames.*;
import static org.muplayer.properties.PropertiesFilesInfo.INFO_FILE_PATH;

@Log
public class Player extends Thread implements PlayerControl {
    private volatile File rootFolder;
    private volatile Track current;

    private final List<Track> listTracks;
    private final List<String> listFolderPaths;
    private final List<PlayerListener> listListeners;

    private final PlayerData playerData;

    private static final int DEFAULT_INITIAL_LIST_CAPACITY = 500;

    static {
        final LogManager logManager = LogManager.getLogManager();
        try {
            logManager.readConfiguration(IOUtil.getArrayStreamFromRes(INFO_FILE_PATH));
        } catch (IOException e) {
            log.warning("Cannot load "+INFO_FILE_PATH);
        }
    }

    public Player() throws FileNotFoundException {
        this((File) null);
    }

    public Player(File rootFolder) throws FileNotFoundException {
        this.rootFolder = rootFolder;
        listTracks = new ArrayList<>(DEFAULT_INITIAL_LIST_CAPACITY);
        listFolderPaths = new ArrayList<>(DEFAULT_INITIAL_LIST_CAPACITY);
        listListeners = new ArrayList<>();
        playerData = new PlayerData();

        checkRootFolder();
        setName("ThreadPlayer "+getId());
    }

    public Player(String folderPath) throws FileNotFoundException {
        this(new File(folderPath));
    }

    private void checkRootFolder() throws FileNotFoundException {
        if(rootFolder != null) {
            if (!rootFolder.exists())
                throw new FileNotFoundException(rootFolder.getPath());
            else {
                loadTracks(rootFolder);
                sortTracks();
            }
        }
    }

    private void loadTracks(File folder) {
        if (!Files.isReadable(folder.toPath()))
            throw new MuPlayerException("folder is not readable");

        final File[] fldFiles = folder.listFiles();
        if (fldFiles != null) {
            // se analiza carpeta y se agregan sonidos recursivamente
            boolean hasTracks = false;
            File file;

            for (int i = 0; i < fldFiles.length; i++) {
                file = fldFiles[i];
                if (file.isDirectory())
                    loadTracks(file);
                else {
                    final Track track = Track.getTrack(FileUtil.getPath(file), this);
                    if (track != null) {
                        listTracks.add(track);
                        hasTracks = true;
                    }
                }
            }

            // si la carpeta tiene sonidos se agrega a la lista de carpetas
            if (hasTracks)
                try {
                    if (listFolderPaths.parallelStream().noneMatch(path-> {
                        try {
                            return path.equals(folder.getCanonicalPath());
                        } catch (IOException e) {
                            return true;
                        }
                    })) {
                        listFolderPaths.add(folder.getCanonicalPath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void loadTracks(List<File> listFiles) {
        listFiles.forEach(file->{
            if (file.isDirectory())
                loadTracks(file);
            else {
                final Track track = Track.getTrack(FileUtil.getPath(file), this);
                if (track != null)
                    listTracks.add(track);
            }
        });
    }

    private void sortTracks() {
        final Comparator<String> comparator = Comparator.naturalOrder();
        listTracks.sort((o1, o2) -> {
            if (o1 == null || o2 == null)
                return 0;
            final File dataSource1 = o1.getDataSourceAsFile();
            final File dataSource2 = o2.getDataSourceAsFile();
            if (dataSource1 == null || dataSource2 == null)
                return 0;
            return FileUtil.getPath(dataSource1).compareTo(FileUtil.getPath(dataSource2));
        });
        listFolderPaths.sort(comparator);
    }

    private int getFolderIndex() {
        final File dataSource = current.getDataSourceAsFile();
        final String currentParent = current != null ? dataSource.getParent() : null;
        return currentParent != null ? listFolderPaths.indexOf(currentParent) : -1;
    }

    private Track getValidTrackBy(int index) {
        Track track = listTracks.get(index);
        try {
            track.validateTrack();
            return track;
        } catch (Exception e) {
            log.info("Error on getTrack: "+(track != null ? track.getTitle() : "Unknown; index="
                    +playerData.getTrackIndex()));
            return null;
        }
    }

    private String getThreadName() {
        File dataSource = current.getDataSourceAsFile();
        final String trackName = dataSource != null ? dataSource.getName() : dataSource.toString();
        final int strLimit = Math.min(trackName.length(), 10);
        return "ThreadTrack: " + trackName.substring(0, strLimit);
    }

    // ojo aqui con los errores que puedan suceder
    private void startTrackThread() {
        if (current != null) {
            current.setName(getThreadName());
            current.setGain(playerData.isMute() ? 0 : playerData.getCurrentVolume());
            current.start();
        }
    }

    private synchronized void freezePlayer() {
        ThreadUtil.freezeThread(this);
    }

    private void shutdownCurrent() {
        if (current != null) {
            current.kill();
            listTracks.set(playerData.getTrackIndex(),
                    Track.getTrack(current.getDataSourceAsFile(), this));
            current = null;
        }
    }

    private void waitForSongs() {
        while (playerData.isOn() && getSongsCount() == 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Se supone que para buscar una cancion a traves de la ruta del padre
    // este ya debe haber sido validado por indexOf para saber
    // si existe o no en la ruta padre
    private TrackSearch findFirstIn(String folderPath) {
        final File parentFile = new File(folderPath);
        Track track;
        for (int i = 0; i < listTracks.size(); i++) {
            track = listTracks.get(i);
            File dataSource = track.getDataSourceAsFile();
            if (dataSource != null && dataSource.getParentFile().equals(parentFile)) {
                return new TrackSearch(track, i);
            }
        }
        return null;
    }

    private int seekToFolder(String folderPath) {
        final File parentFile = new File(folderPath);
        File fileTrack;
        final int trackCount = listTracks.size();
        int newTrackIndex = -1;

        // idea para electrolist -> Indexof con predicate
        Track track;
        for (int i = 0; i < trackCount; i++) {
            track = listTracks.get(i);
            fileTrack = track.getDataSourceAsFile();
            if (fileTrack != null && fileTrack.getParentFile().equals(parentFile)) {
                newTrackIndex = i;
                break;
            }
        }
        return newTrackIndex;
    }

    private void startPlaying() {
        playerData.setOn(true);
        waitForSongs();
        playNext();
    }

    private void playFolderSongs(String fldPath) {
        Track track;
        File dataSource;
        for (int i = 0; i < listTracks.size(); i++) {
            track = listTracks.get(i);
            dataSource = track.getDataSourceAsFile();
            if (dataSource != null && dataSource.getParent().equals(fldPath)) {
                play(i);
                break;
            }
        }
    }

    private synchronized void changeTrack(SeekOption seekOption) {
        final int currentIndex = playerData.getTrackIndex();
        final int newIndex;
        if (seekOption == SeekOption.NEXT)
            newIndex = currentIndex == listTracks.size() - 1 ? 0 : currentIndex+1;
        else
            newIndex = currentIndex == 0 ? listTracks.size()-1 : currentIndex-1;
        changeTrack(newIndex);
    }

    private synchronized void changeTrack(int trackIndex) {
        shutdownCurrent();
        playerData.setTrackIndex(trackIndex);
        current = getValidTrackBy(playerData.getTrackIndex());
        startTrackThread();
        loadListenerMethod(ONSONGCHANGE, current);
    }

    private synchronized void changeTrack(TrackSearch trackSearch) {
        if (trackSearch != null) {
            shutdownCurrent();
            playerData.setTrackIndex(trackSearch.getIndex());
            current = trackSearch.getTrack();
            startTrackThread();
            loadListenerMethod(ONSONGCHANGE, current);
        }
    }

    public void loadListenerMethod(String methodName, Track track) {
        if (!listListeners.isEmpty())
            TaskRunner.execute(new ListenerRunner(listListeners, methodName, track));
    }

    public boolean isActive() {
        return isAlive() && hasSounds();
    }

    public boolean existsFolder(String folderPath) {
        return listFolderPaths.parallelStream().anyMatch(fp->fp.equals(folderPath));
    }

    public boolean existsFolder(File folder) {
        return existsFolder(folder.getPath());
    }

    public boolean existsParent(String childPath) {
        return existsParent(new File(childPath));
    }

    public boolean existsParent(File child) {
        return existsFolder(child.getParent());
    }

    public TrackState getCurrentTrackState() {
        return current == null ? new UnknownState(current) : current.getTrackState();
    }

    public String getCurrentTrackStateToString() {
        return current == null ? "Unknown" : current.getStateToString();
    }

    public int getFoldersCount() {
        return listFolderPaths.size();
    }

    public File getRootFolder() {
        return rootFolder;
    }

    public List<String> getListFolderPaths() {
        return listFolderPaths;
    }

    public synchronized void jumpTrack(int jumps, SeekOption option) {
        if (option == SeekOption.NEXT) {
            playerData.increaseTrackIndex(jumps);
            if (playerData.getTrackIndex() >= listTracks.size())
                playerData.setTrackIndex(0);
        }
        else {
            playerData.decreaseTrackIndex(jumps);
            if (playerData.getTrackIndex() < 0)
                playerData.setTrackIndex(listTracks.size()-1);
        }
        play(playerData.getTrackIndex());
    }

    public synchronized List<File> getListSoundFiles() {
        return listTracks.stream().filter(track -> track.getDataSourceAsFile() != null)
                .map(track -> (File)track.getDataSourceAsFile()).collect(Collectors.toList());
    }

    // Se supone que todos los tracks serian validos
    // sino rescatar de los que sean no mas
    public synchronized List<AudioTag> getTrackTags() {
        final List<AudioTag> listTags = new LinkedList<>();
        listTracks.forEach(track -> {
            try {
                final AudioTag tag = new AudioTag(track.getDataSourceAsFile());
                if (tag.isValidFile())
                    listTags.add(tag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return listTags;
    }

    public synchronized List<Track> getTracks() {
        return listTracks;
    }

    public synchronized List<TrackInfo> getTracksInfo() {
        final List<TrackInfo> listInfo = new ArrayList<>(listTracks.size()+1);
        listTracks.forEach(track->{
            try {
                final AudioTag tag = new AudioTag(track.getDataSourceAsFile());
                if (tag.isValidFile())
                    listInfo.add(new SongData(tag));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return listInfo;
    }

    public synchronized List<Artist> getArtists() {
        final List<TrackInfo> listTracks = getTracksInfo();
        final List<Artist> listArtists = new ArrayList<>(listTracks.size()+1);

        listTracks.parallelStream()
                .forEach(track->{
                    String art = track.getArtist();
                    if (art == null)
                        art = "Unknown";
                    synchronized (listArtists) {
                        String finalArt = art;
                        Artist artist = listArtists.parallelStream()
                                .filter(a->a.getName().equalsIgnoreCase(finalArt))
                                .findFirst().orElse(null);

                        if (artist == null) {
                            artist = new Artist();
                            artist.setName(finalArt);
                            listArtists.add(artist);
                        }
                        artist.addTrack(track);
                    }
                });
        listArtists.sort(Comparator.comparing(Artist::getName));
        return listArtists;
    }

    public synchronized List<Album> getAlbums() {
        final List<TrackInfo> listTracks = getTracksInfo();
        final List<Album> listAlbums = new ArrayList<>(listTracks.size()+1);

        listTracks.parallelStream()
                .forEach(track->{
                    String alb = track.getAlbum();
                    if (alb == null)
                        alb = "Unknown";
                    synchronized (listAlbums) {
                        String finalAlb = alb;
                        Album album = listAlbums.parallelStream()
                                .filter(a->a.getName().equalsIgnoreCase(finalAlb)).findFirst().orElse(null);

                        if (album == null) {
                            album = new Album();
                            album.setName(finalAlb);
                            listAlbums.add(album);
                        }
                        album.addTrack(track);
                    }
                });
        listAlbums.sort(Comparator.comparing(Album::getName));
        return listAlbums;
    }

    public synchronized void addPlayerListener(PlayerListener listener) {
        listListeners.add(listener);
    }

    public synchronized List<PlayerListener> getListeners() {
        return listListeners;
    }

    public synchronized void removePlayerListener(PlayerListener reference) {
        listListeners.removeIf(listener->listener.equals(reference));
    }

    public synchronized void removeAllListeners() {
        listListeners.clear();
    }

    // vuelve a leer y cargar las tracks
    public synchronized void reloadTracks() {
        if (rootFolder != null) {
            final int currentIndex = playerData.getTrackIndex();
            listTracks.clear();
            listFolderPaths.clear();
            loadTracks(rootFolder);
            sortTracks();

            final int songCount = getSongsCount();
            playerData.setTrackIndex(songCount > currentIndex ? currentIndex : songCount-1);
        }
    }

    public PlayerInfo getInfo() {
        return new PlayerInfo(this);
    }

    public synchronized Track getCurrent() {
        return current;
    }

    public synchronized TrackInfo getNext() {
        final int trackIndex = playerData.getTrackIndex();
        final int songsCount = getSongsCount();
        final int nextIndex = trackIndex == -1 ? 0 : (trackIndex == songsCount-1 ? 0 : trackIndex+1);
        return listTracks.get(nextIndex);
    }

    public synchronized TrackInfo getPrevious() {
        final int trackIndex = playerData.getTrackIndex();
        final int songsCount = getSongsCount();
        final int prevIndex = trackIndex == -1 ? songsCount-1 : (trackIndex == 0 ? songsCount-1 : trackIndex-1);
        return listTracks.get(prevIndex);
    }

    public int getSongsCount() {
        return listTracks.size();
    }

    @Override
    public boolean hasSounds() {
        return !listTracks.isEmpty();
    }

    @Override
    public synchronized boolean isOn() {
        return playerData.isOn();
    }

    @Override
    public synchronized void turnOn() {
        start();
    }

    @Override
    public synchronized boolean isPlaying() {
        return current != null && current.isPlaying();
    }

    @Override
    public synchronized boolean isPaused() {
        return current != null && current.isPaused();
    }

    @Override
    public synchronized boolean isStopped() {
        return current != null && current.isStopped();
    }

    @Override
    public synchronized boolean isFinished() {
        return current != null && current.isFinished();
    }

    @Override
    public synchronized boolean isMute() {
        return playerData.isMute();
    }

    // ojo cuando se agrega musica de carpetas que estan fuera de rootFolder
    // puede ocasionar problemas
    @Override
    public synchronized void addMusic(Collection<File> soundCollection) {
        if (!soundCollection.isEmpty())
            soundCollection.forEach(this::loadTracks);
    }

    @Override
    public synchronized void addMusic(File folderOrFile) {
        if (folderOrFile.isDirectory()) {
            if (rootFolder == null)
                rootFolder = folderOrFile;
            final boolean validSort = !hasSounds();
            loadTracks(folderOrFile);
            if (validSort)
                sortTracks();
        }
        else if (AudioUtil.isSupportedFile(folderOrFile)) {
            final Track track = Track.getTrack(folderOrFile, this);
            if (track != null) {
                listTracks.add(track);
                final String parentPath = folderOrFile.getParent();
                if (listFolderPaths.parallelStream().noneMatch(parentPath::equals))
                    listFolderPaths.add(parentPath);
            }
        }
    }

    @Override
    public synchronized void seekFolder(SeekOption param) {
        seekFolder(param, 1);
    }

    @Override
    public synchronized void seekFolder(SeekOption option, int jumps) {
        final int folderIndex = getFolderIndex();
        if (folderIndex != -1) {
            final int newFolderIndex;
            final String parentToFind;

            if (option == SeekOption.NEXT) {
                newFolderIndex = folderIndex+jumps;
                parentToFind = listFolderPaths.get(newFolderIndex >= getFoldersCount()
                        ? 0 : newFolderIndex);
            }
            else {
                newFolderIndex = folderIndex - jumps;
                parentToFind = listFolderPaths.get(newFolderIndex < 0
                        ? listFolderPaths.size()-1 : newFolderIndex);
            }
            changeTrack(findFirstIn(parentToFind));
        }
    }

    @Override
    public synchronized void play() {
        if (!isAlive())
            start();
        else if (current != null) {
            current.play();
            loadListenerMethod(ONPLAYED, current);
        }
    }

    @Override
    public void play(int index) {
        if (current != null)
            shutdownCurrent();
        if (index > -1 && index < getSongsCount()) {
            final Track track = listTracks.get(index);
            if (track != null) {
                current = track;
                startTrackThread();
                playerData.setTrackIndex(index);
                loadListenerMethod(ONPLAYED, current);
            }
        }
    }

    // Reproduce archivo de audio en la lista
    // (is alive)
    @Override
    public synchronized void play(File track) {
        final int indexOf = listTracks.indexOf(listTracks.parallelStream().filter(
                t->t.getDataSourceAsFile() != null &&
                        ((File)t.getDataSourceAsFile()).getPath().equals(track.getPath()))
                .findFirst().orElse(null));
        if (indexOf == -1) {
            if (AudioUtil.isSupportedFile(track)) {
                listTracks.add(Track.getTrack(track, this));
                if (!existsFolder(track.getParent()))
                    listFolderPaths.add(track.getParent());
            }
        }
        else {
            playerData.setTrackIndex(indexOf);
            if (current != null)
                current.kill();
            current = listTracks.get(playerData.getTrackIndex());
            startTrackThread();
            loadListenerMethod(ONPLAYED, current);
        }
    }

    @Override
    public synchronized void play(String trackName) {
        int indexOf = -1;
        File trackFile = null;
        Track track = null;

        for (int i = 0; i < listTracks.size(); i++) {
            track = listTracks.get(i);
            trackFile = track.getDataSourceAsFile();
            if (trackFile != null && trackFile.getName().equals(trackName)) {
                indexOf = i;
                break;
            }
            trackFile = null;
            track = null;
        }

        if (indexOf != -1) {
            playerData.setTrackIndex(indexOf);
            if (current != null)
                current.kill();
            current = track;
            startTrackThread();
            loadListenerMethod(ONPLAYED, current);
        }
    }

    @Override
    public synchronized void pause() {
        if (current != null) {
            current.pause();
            loadListenerMethod(ONPAUSED, current);
        }
    }

    @Override
    public synchronized void resumeTrack() {
        if (current != null) {
            current.resumeTrack();
            loadListenerMethod(ONRESUMED, current);
        }
    }

    @Override
    public synchronized void stopTrack() {
        if (current != null) {
            current.stopTrack();
            loadListenerMethod(ONSTOPPED, current);
        }
    }

    @Override
    public synchronized void finish() {
        shutdown();
    }

    @Override
    public synchronized void seek(double seconds) {
        if (current != null) {
            try {
                current.seek(seconds);
                loadListenerMethod(ONGOTOSECOND, current);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void gotoSecond(double second) {
        if (current != null) {
            try {
                current.gotoSecond(second);
                loadListenerMethod(ONGOTOSECOND, current);
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized float getGain() {
        return current == null
                ? playerData.getCurrentVolume() : current.getGain();
    }

    // 0-100
    @Override
    public synchronized void setGain(float volume) {
        playerData.setCurrentVolume(volume);
        if (current != null)
            current.setGain(volume);
        playerData.setMute(playerData.getCurrentVolume() == 0);
    }

    @Override
    public float getSystemVolume() {
        return AudioHardware.getFormattedMasterVolume();
    }

    @Override
    public void setSystemVolume(float volume) {
        AudioHardware.setFormattedMasterVolume(volume);
    }

    @Override
    public synchronized void mute() {
        playerData.setMute(true);
        if (current != null)
            current.mute();
    }

    @Override
    public synchronized void unMute() {
        playerData.setMute(false);
        if (current != null)
            current.setGain(playerData.getCurrentVolume());
        else
            playerData.setCurrentVolume(100);
    }

    @Override
    public double getProgress() {
        return current == null ? 0 : current.getProgress();
    }

    @Override
    public long getDuration() {
        return listTracks.stream().map(Track::getDuration)
                .reduce(Long::sum).orElse(0L);
    }

    @Override
    public synchronized void playNext() {
        changeTrack(SeekOption.NEXT);
    }

    @Override
    public synchronized void playPrevious() {
        changeTrack(SeekOption.PREV);
    }

    @Override
    public void playFolder(String path) {
        shutdownCurrent();
        playFolderSongs(path);
    }

    @Override
    public void playFolder(int folderIndex) {
        final int foldersCount = getFoldersCount();
        if (folderIndex >= foldersCount)
            folderIndex = foldersCount-1;
        final int newIndex = seekToFolder(listFolderPaths.get(folderIndex));
        changeTrack(newIndex);
    }

    @Override
    public synchronized void shutdown() {
        playerData.setOn(false);
        shutdownCurrent();
        this.interrupt();
        loadListenerMethod(ONSHUTDOWN, null);
    }

    @Override
    public synchronized void run() {
        loadListenerMethod(ONSTARTED, null);
        startPlaying();
        freezePlayer();
    }

}
