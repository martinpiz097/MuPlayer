package org.muplayer.audio;

import org.aucom.sound.Speaker;
import org.muplayer.audio.info.AudioTag;
import org.muplayer.audio.info.SongData;
import org.muplayer.audio.interfaces.PlayerControls;
import org.muplayer.audio.interfaces.PlayerListener;
import org.muplayer.audio.model.Album;
import org.muplayer.audio.model.Artist;
import org.muplayer.audio.model.SeekOption;
import org.muplayer.audio.model.TrackInfo;
import org.muplayer.audio.trackstates.TrackState;
import org.muplayer.audio.trackstates.UnknownState;
import org.muplayer.audio.util.PlayerInfo;
import org.muplayer.exception.MuPlayerException;
import org.muplayer.system.AudioUtil;
import org.muplayer.system.LineUtil;
import org.muplayer.thread.ListenerRunner;
import org.muplayer.thread.TaskRunner;
import org.muplayer.thread.ThreadManager;
import org.orangelogger.sys.Logger;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.muplayer.system.ListenersNames.*;

public class Player extends Thread implements PlayerControls {
    private volatile File rootFolder;
    private volatile Track current;

    private final List<Track> listTracks;
    private final List<String> listFolderPaths;
    private final List<PlayerListener> listListeners;

    private volatile int trackIndex;
    private volatile float currentVolume;
    private volatile boolean on;
    private volatile boolean isMute;

    public static final float DEFAULT_VOLUME = AudioUtil.convertLineRangeToVolRange(AudioUtil.MiDDLE_VOL);

    public Player() throws FileNotFoundException {
        this((File) null);
    }

    public Player(File rootFolder) throws FileNotFoundException {
        this.rootFolder = rootFolder;
        listTracks = new ArrayList<>();
        listFolderPaths = new ArrayList<>();
        listListeners = new ArrayList<>();
        currentVolume = DEFAULT_VOLUME;
        on = false;
        isMute = false;

        checkRootFolder();
        setName("ThreadPlayer "+getId());
        trackIndex = -1;
        disableLogging();
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

    public Player(String folderPath) throws FileNotFoundException {
        this(new File(folderPath));
    }

    private void disableLogging() {
        java.util.logging.Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
    }

    // Problema con ogg al leer tagInfo archivo

    // no se revisaran si los archivos son sonidos por
    // ahora porque se piensa en reducir tiempos de carga
    // haciendo de la revision en tiempo de ejecucion

    private void loadTracks(File folder) {
        if (!Files.isReadable(folder.toPath()))
            throw new MuPlayerException("folder is not readable");

        //Files.list(folder.toPath()).forEach(path->{});
        final File[] fldFiles = folder.listFiles();
        File f;

        if (fldFiles != null) {
            // se analiza carpeta y se agregan sonidos recursivamente
            boolean hasTracks = false;
            String filePath;

            for (int i = 0; i < fldFiles.length; i++) {
                f = fldFiles[i];
                if (f.isDirectory())
                    loadTracks(f);
                else {
                    try {
                        filePath = f.getCanonicalPath();
                        if (Track.isValidTrack(filePath)) {
                            final Track track = Track.getTrack(filePath, this);
                            if (track != null) {
                                listTracks.add(track);
                                hasTracks = true;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
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
        listFiles.forEach(f->{
            if (f.isDirectory())
                loadTracks(f);
            else {
                try {
                    final String filePath = f.getCanonicalPath();
                    if (Track.isValidTrack(filePath)) {
                        listTracks.add(Track.getTrack(filePath, this));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sortTracks() {
        final Comparator<String> comparator = Comparator.naturalOrder();
        listTracks.sort(new Comparator<Track>() {
            @Override
            public int compare(Track o1, Track o2) {
                if (o1 == null || o2 == null)
                    return 0;
                File dataSource1 = o1.getDataSource();
                File dataSource2 = o2.getDataSource();
                if (dataSource1 == null || dataSource2 == null)
                    return 0;
                try {
                    return dataSource1.getCanonicalPath().compareTo(dataSource2.getCanonicalPath());
                } catch (IOException e) {
                    return 0;
                }
            }
        });
        listFolderPaths.sort(comparator);
    }

    private int getFolderIndex() {
        final String currentParent = current != null ? current.getDataSource().getParent() : null;
        return currentParent != null ? listFolderPaths.indexOf(currentParent) : -1;
    }

    private Track getTrackBy(int currentIndex, SeekOption param) {
        Track nextTrack = null;
        int nextIndex;
        if (param == SeekOption.NEXT) {
            nextIndex = currentIndex == getSongsCount()-1 || currentIndex < 0 ? 0 : currentIndex+1;

            for (int i = nextIndex; i < listTracks.size(); i++) {
                nextTrack = listTracks.get(i);
                // Este if es por si existen archivos que no fuesen sonidos
                // en las carpetas
                if (nextTrack != null) {
                    try {
                        nextTrack.validateTrack();
                        trackIndex = i;
                        break;
                    } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {

                    }
                }
            }
        }

        else {
            nextIndex = currentIndex == 0 ? getSongsCount()-1 : currentIndex-1;

            for (int i = nextIndex; i >= 0; i--) {
                nextTrack = listTracks.get(i);
                if (nextTrack != null) {
                    try {
                        nextTrack.validateTrack();
                        trackIndex = i;
                        break;
                    } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {

                    }
                }
            }
        }
        return nextTrack;
    }

    private String getThreadName() {
        final String trackName = current.getDataSource().getName();
        final int strLimit = Math.min(trackName.length(), 10);
        return new StringBuilder("ThreadTrack: ")
                .append(trackName, 0, strLimit).toString();
    }

    // ojo aqui con los errores que puedan suceder
    private void startTrackThread() {
        if (current != null) {
            current.setName(getThreadName());
            current.setGain(isMute ? 0 : currentVolume);
            current.start();
        }
    }

    public void loadListenerMethod(String methodName, Track track) {
        if (!listListeners.isEmpty())
            TaskRunner.execute(new ListenerRunner(listListeners, methodName, track));
    }

    private synchronized void freezePlayer() {
        ThreadManager.freezeThread(this);
    }

    private void shutdownCurrent() {
        if (current != null) {
            current.kill();
            listTracks.set(trackIndex, Track.getTrack(current.getDataSource(), this));
        }
    }

    private void waitForSongs() {
        while (on && getSongsCount() == 0) {
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
    private Track findFirstIn(String folderPath) {
        final File parentFile = new File(folderPath);
        return listTracks.stream()
                .filter(file -> file.getDataSource().getParentFile().equals(parentFile))
                .findFirst().orElse(null);
    }

    private int seekToFolder(String folderPath) {
        final File parentFile = new File(folderPath);
        final List<File> listSounds = getListSounds();

        File fileTrack;
        int trackIndex = -1;

        // idea para electrolist -> Indexof con predicate
        for (int i = 0; i < listSounds.size(); i++) {
            fileTrack = listSounds.get(i);
            if (fileTrack.getParentFile().equals(parentFile)) {
                trackIndex = i;
                break;
            }
        }
        return trackIndex-1;

    }

    private void startPlaying() {
        on = true;
        waitForSongs();
        playNext();
    }

    private void playFolderSongs(String fldPath) {
        for (int i = 0; i < listTracks.size(); i++) {
            if (listTracks.get(i).getDataSource().getParent().equals(fldPath)) {
                play(i);
                break;
            }
        }
    }

    public boolean hasSounds() {
        return !listTracks.isEmpty();
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

    // Test
    // revisar
    public synchronized void jumpTrack(int jumps, SeekOption option) {
        if (option == SeekOption.NEXT) {
            trackIndex+=jumps;
            if (trackIndex >= listTracks.size())
                trackIndex = 0;
        }
        else {
            trackIndex -= jumps;
            if (trackIndex < 0)
                trackIndex = listTracks.size()-1;
        }
        play(trackIndex);
    }


    public synchronized List<File> getListSounds() {
        return listTracks.stream().map(Track::getDataSource).collect(Collectors.toList());
    }

    // Se supone que todos los tracks serian validos
    // sino rescatar de los que sean no mas
    public synchronized List<AudioTag> getTrackTags() {
        final List<AudioTag> listTags = new LinkedList<>();
        listTracks.forEach(track -> {
            try {
                final AudioTag tag = new AudioTag(track.getDataSource());
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
        final List<TrackInfo> listInfo = new LinkedList<>();
        listTracks.forEach(track->{
            try {
                AudioTag tag = new AudioTag(track.getDataSource());
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
        final List<Artist> listArtists = new LinkedList<>();

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
        final List<Album> listAlbums = new LinkedList<>();

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

    public synchronized void reloadTracks() {
        if (rootFolder != null) {
            final int currentIndex = trackIndex;
            listFolderPaths.clear();
            loadTracks(rootFolder);
            sortTracks();

            final int songCount = getSongsCount();
            if (songCount > currentIndex)
                trackIndex = currentIndex;
        }
    }

    public PlayerInfo getInfo() {
        return new PlayerInfo(this);
    }

    public synchronized Track getCurrent() {
        return current;
    }

    public synchronized TrackInfo getNext() {
        final int songsCount = getSongsCount();
        final int nextIndex = trackIndex == -1 ? 0 : (trackIndex == songsCount-1 ? 0 : trackIndex+1);
        return listTracks.get(nextIndex);
    }

    public synchronized TrackInfo getPrevious() {
        final int songsCount = getSongsCount();
        final int prevIndex = trackIndex == -1 ? 0 : (trackIndex == 0 ? songsCount-1 : trackIndex-1);
        return listTracks.get(prevIndex);
    }

    public synchronized Speaker getTrackSpeaker() {
        return current.getTrackLine();
    }

    public synchronized SourceDataLine getTrackLine() {
        return current == null ? null :
                (current.getTrackLine() == null ?
                        null : current.getTrackLine().getDriver());
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
        //return current != null && current.isMute();
        return isMute;
    }

    @Override
    public synchronized void open(File sound) {
        if (!Track.isValidTrack(sound))
            return;
        listTracks.clear();
        listFolderPaths.clear();
        listTracks.add(Track.getTrack(sound, this));
        listFolderPaths.add(sound.getParent());
        if (isPlaying())
            current.finish();
        else if (isAlive()) {
            current = Track.getTrack(sound, this);
            startTrackThread();
        }
        else
            start();
    }

    @Override
    public synchronized void open(List<File> listSounds) {
        final List<File> listValidSounds = listSounds.parallelStream()
                .filter(Track::isValidTrack)
                .collect(Collectors.toList());

        if (!listValidSounds.isEmpty()) {
            listTracks.clear();
            listFolderPaths.clear();
            loadTracks(listValidSounds);
            sortTracks();
        }
    }

    @Override
    public synchronized void addMusic(Collection<File> soundCollection) {
        if (!soundCollection.isEmpty())
            soundCollection.forEach(this::loadTracks);
    }

    @Override
    public synchronized void addMusic(File musicFolder) {
        if (musicFolder.isDirectory()) {
            if (rootFolder == null)
                rootFolder = musicFolder;
            boolean validSort = !hasSounds();
            loadTracks(musicFolder);
            if (validSort)
                sortTracks();
        }
        else if (Track.isValidTrack(musicFolder)) {
            Track track = Track.getTrack(musicFolder, this);
            if (track != null) {
                listTracks.add(track);
                final String parentPath = musicFolder.getParent();
                if (listFolderPaths.parallelStream().noneMatch(parentPath::equals))
                    listFolderPaths.add(parentPath);
            }
        }
    }

    public int getSongsCount() {
        return listTracks.size();
    }

    public synchronized void seekFolder(SeekOption param) {
        seekFolder(param, 1);
    }

    public synchronized void seekFolder(SeekOption option, int jumps) {
        final int folderIndex = getFolderIndex();
        if (folderIndex != -1) {
            int newFolderIndex;
            String parentToFind;
            Track next;

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

            next = findFirstIn(parentToFind);
            if (next != null) {
                try {
                    trackIndex = listTracks.indexOf(next.getDataSource().getCanonicalPath());
                    if (current != null)
                        current.kill();
                    current = next;
                    startTrackThread();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

    public void play(int index) {
        if (current != null && (index > -1 && index < getSongsCount())) {
            current.kill();
            final Track track = listTracks.get(index);
            if (track != null) {
                current = track;
                startTrackThread();
                trackIndex = index;
                loadListenerMethod(ONPLAYED, current);
            }
        }
    }

    // Reproduce archivo de audio en la lista
    // (is alive)
    @Override
    public synchronized void play(File track) {
        final int indexOf = listTracks.indexOf(listTracks.parallelStream().filter(
                t->t.getDataSource().getPath().equals(t.getDataSource().getPath()))
                .findFirst().orElse(null));
        if (indexOf == -1) {
            if (Track.isValidTrack(track)) {
                listTracks.add(Track.getTrack(track, this));
                if (!existsFolder(track.getParent()))
                    listFolderPaths.add(track.getParent());
            }
        }
        else {
            trackIndex = indexOf;
            if (current != null)
                current.kill();
            current = listTracks.get(trackIndex);
            startTrackThread();
            loadListenerMethod(ONPLAYED, current);
        }
    }

    @Override
    public synchronized void play(String trackName) {
        int indexOf = -1;
        File song = null;
        Track track = null;

        for (int i = 0; i < listTracks.size(); i++) {
            track = listTracks.get(i);
            song = track.getDataSource();
            if (song.getName().equals(trackName)) {
                indexOf = i;
                break;
            }
            song = null;
            track = null;
        }

        if (indexOf != -1) {
            trackIndex = indexOf;
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
    public synchronized void stopTrack() throws Exception {
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
        return current == null ? currentVolume : current.getGain();
    }

    // 0-100
    @Override
    public synchronized void setGain(float volume) {
        currentVolume = volume;
        if (current != null)
            current.setGain(volume);
        isMute = currentVolume == 0;
    }

    @Override
    public float getSystemVolume() {
        return LineUtil.getFormattedMasterVolume();
    }

    @Override
    public void setSystemVolume(float volume) {
        LineUtil.setFormattedMasterVolume(volume);
    }

    @Override
    public synchronized void mute() {
        isMute = true;
        if (current != null)
            current.mute();
    }

    @Override
    public synchronized void unmute() {
        isMute = false;
        if (current != null)
            current.setGain(currentVolume);
        else
            currentVolume = 100;
    }

    public double getProgress() {
        return current == null ? 0 : current.getProgress();
    }

    public String getFormattedProgress() {
        return current == null ? "00:00" : current.getFormattedProgress();
    }

    private synchronized void changeTrack(SeekOption seekOption) {
        shutdownCurrent();
        current = getTrackBy(trackIndex, seekOption);
        startTrackThread();
        loadListenerMethod(ONSONGCHANGE, current);
    }

    @Override
    public synchronized void playNext() {
        changeTrack(SeekOption.NEXT);
    }

    @Override
    public synchronized void playPrevious() {
        changeTrack(SeekOption.PREV);
    }

    public void playFolder(String path) {
        if (current != null)
            current.kill();

        if (listFolderPaths.contains(path))
            playFolderSongs(path);
    }

    public void playFolder(int index) {
        if (index < getFoldersCount()) {
            trackIndex = seekToFolder(listFolderPaths.get(index));
            playNext();
        }
    }

    @Override
    public synchronized void shutdown() {
        on = false;
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
