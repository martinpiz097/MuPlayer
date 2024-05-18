package org.muplayer.audio.player;

import lombok.extern.java.Log;
import org.muplayer.audio.track.Track;
import org.muplayer.audio.track.states.TrackState;
import org.muplayer.audio.track.states.UnknownState;
import org.muplayer.exception.MuPlayerException;
import org.muplayer.listener.PlayerListener;
import org.muplayer.model.*;
import org.muplayer.thread.*;
import org.muplayer.util.*;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.muplayer.listener.ListenersNames.*;
import static org.muplayer.thread.ThreadUtil.generateTrackThreadName;

@Log
public class MusicPlayer extends Player {
    private volatile File rootFolder;
    private volatile Track current;

    private final List<Track> listTracks;
    private final List<File> listFolders;
    private final List<PlayerListener> listListeners;

    private final PlayerData playerData;

    public MusicPlayer() throws FileNotFoundException {
        this((File) null);
    }

    public MusicPlayer(File rootFolder) throws FileNotFoundException {
        this.rootFolder = rootFolder;
        listTracks = CollectionUtil.newFastList();
        listFolders = CollectionUtil.newFastList();
        listListeners = CollectionUtil.newFastList();
        playerData = new PlayerData();

        checkRootFolder();
        setName("MusicPlayer " + getId());
    }

    public MusicPlayer(String folderPath) throws FileNotFoundException {
        this(new File(folderPath));
    }

    private void checkRootFolder() throws FileNotFoundException {
        if (rootFolder != null && rootFolder.exists()) {
            setupTracksList();
        } else if (rootFolder == null) {
            throw new MuPlayerException("The root folder is null");
        } else {
            throw new FileNotFoundException(rootFolder.getPath());
        }
    }

    private void loadTracks(File folderToLoad) {
        CompletableFuture<Void> completableFuture = TracksLoader.getInstance().addTask(() -> {
            if (!Files.isReadable(folderToLoad.toPath())) {
                throw new MuPlayerException("folderToLoad " + folderToLoad.getPath() + " is not readable");
            }
            final File[] fldFiles = folderToLoad.listFiles();
            if (fldFiles != null) {
                // se analiza carpeta y se agregan sonidos recursivamente
                AtomicBoolean hasTracks = new AtomicBoolean(false);
                List<File> listFiles = new LinkedList<>(Arrays.asList(fldFiles));

                listFiles.parallelStream()
                        .forEach(file -> {
                            if (file.isDirectory()) {
                                loadTracks(file);
                            } else {
                                final Track track = Track.getTrack(FileUtil.getPath(file), this);
                                if (track != null) {
                                    synchronized (hasTracks) {
                                        hasTracks.set(true);
                                    }
                                    synchronized (listTracks) {
                                        listTracks.add(track);
                                    }
                                }
                            }
                        });

                // si la carpeta tiene sonidos se agrega a la lista de carpetas
                if (hasTracks.get()) {
                    synchronized (listFolders) {
                        listFolders.add(folderToLoad);
                    }
                }
            }
        });

        new TracksLoaderCleaner(completableFuture).start();
    }

    private void loadTracks(List<File> listFiles) {
        listFiles.parallelStream().forEach(file -> {
            if (file.isDirectory()) {
                loadTracks(file);
                waitForTracksLoading();
            }
            else {
                final Track track = Track.getTrack(FileUtil.getPath(file), this);
                if (track != null) {
                    listTracks.add(track);
                }
            }
        });
    }

    private void waitForTracksLoading() {
        while (TracksLoader.getInstance().hasPendingTasks()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        }
    }

    private void sortTracks() {
        listTracks.sort((o1, o2) -> {
            if (o1 == null || o2 == null) {
                return 0;
            }
            final File dataSource1 = o1.getDataSourceAsFile();
            final File dataSource2 = o2.getDataSourceAsFile();
            if (dataSource1 == null || dataSource2 == null) {
                return 0;
            }
            return dataSource1.getPath().compareTo(dataSource2.getPath());
        });
        listFolders.sort(Comparator.comparing(File::getPath));
    }

    private void setupTracksList() {
        setupTracksList(rootFolder);
    }

    private void setupTracksList(File folder) {
        TimeTester.measureTaskTime(TimeUnit.MILLISECONDS, "loadTracks task: ", () -> loadTracks(folder));
        TimeTester.measureTaskTime(TimeUnit.MILLISECONDS, "waitForTracksLoading task: ", this::waitForTracksLoading);
        TimeTester.measureTaskTime(TimeUnit.MILLISECONDS, "sortTracks task: ", this::sortTracks);
    }

    private int getFolderIndex() {
        final File dataSource = current.getDataSourceAsFile();
        final File currentParent = current != null ? dataSource.getParentFile() : null;
        return currentParent != null ? listFolders.indexOf(currentParent) : -1;
    }

    // ojo aqui con los errores que puedan suceder
    private void startTrackThread() {
        if (current != null) {
            current.setName(generateTrackThreadName(current.getClass(), current));
            current.setVolume(playerData.getVolume());
            if (isMute()) {
                current.mute();
            }
            current.start();
        }
    }

    private synchronized void freezePlayer() {
        ThreadUtil.freezeThread(this);
    }

    private void restartCurrent() {
        try {
            if (current != null) {
                Track cur = Track.getTrack(current.getDataSource());
                current.kill();
                listTracks.set(playerData.getTrackIndex(), cur);
            }
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
    }

    private void waitForSongs() {
        int songsCount;
        while (playerData.isOn() && (songsCount = getSongsCount()) == 0) {
            try {
                log.info("WaitForSongs::Songs count: " + songsCount);
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // si incluyo paralelismo en este metodo, debo crear otro o gestionar con parametro boolean,
    // ya que hay algunos casos en los que si necesito secuencialidad
    private TrackIndexed getTrackIndexedFromCondition(Predicate<Track> filter) {
        int index = 0;
        for (Track track : listTracks) {
            if (filter.test(track)) {
                return new TrackIndexed(track, index);
            }
            index++;
        }
        return null;
    }

    // Se supone que para buscar una cancion a traves de la ruta del padre
    // este ya debe haber sido validado por indexOf para saber
    // si existe o no en la ruta padre
    private TrackIndexed findFirstIn(String folderPath) {
        final File parentFile = new File(folderPath);
        final Predicate<Track> filter = FilterUtil.getFindFirstInFilter(parentFile);

        return getTrackIndexedFromCondition(filter);
    }

    private int seekToFolder(String folderPath) {
        final File parentFile = new File(folderPath);
        int newTrackIndex = -1;

        // idea para electrolist -> Indexof con predicate
        Predicate<Track> filter = FilterUtil.newSeekToFolderFilter(parentFile);
        TrackIndexed trackIndexed = getTrackIndexedFromCondition(filter);

        if (trackIndexed != null) {
            newTrackIndex = trackIndexed.getIndex();
        }
        return newTrackIndex;
    }

    private void startPlaying() {
        playerData.setOn(true);
        waitForSongs();
        playNext();
    }

    private void playFolderSongs(String fldPath) {
        Predicate<Track> filter = FilterUtil.getPlayFolderFilter(fldPath);
        TrackIndexed trackIndexed = getTrackIndexedFromCondition(filter);

        if (trackIndexed != null) {
            play(trackIndexed);
        }
    }

    private synchronized void changeTrack(SeekOption seekOption) {
        final int currentIndex = playerData.getTrackIndex();
        final int newIndex;
        if (seekOption == SeekOption.NEXT)
            newIndex = currentIndex == listTracks.size() - 1 ? 0 : currentIndex + 1;
        else
            newIndex = currentIndex == 0 ? listTracks.size() - 1 : currentIndex - 1;
        changeTrack(newIndex);
    }

    private synchronized void changeTrack(int newTrackIndex) {
        Track track = listTracks.get(newTrackIndex);
        if (track != null) {
            TrackIndexed trackIndexed = new TrackIndexed(track, newTrackIndex);
            changeTrack(trackIndexed);
        }
    }

    private synchronized void changeTrack(TrackIndexed trackIndexed) {
        if (trackIndexed != null) {
            restartCurrent();
            playerData.setTrackIndex(trackIndexed.getIndex());
            current = trackIndexed.getTrack();
            startTrackThread();
            loadListenerMethod(ONSONGCHANGE, current);
        }
    }

    public void loadListenerMethod(String methodName, Track track) {
        if (!listListeners.isEmpty())
            TaskRunner.execute(new ListenerRunner(listListeners, methodName, track), "ListenerRunner");
    }

    private boolean existsFolder(String folderPath) {
        return listFolders.parallelStream().anyMatch(fp -> fp.getPath().equals(folderPath));
    }

    @Override
    public TrackState getCurrentTrackState() {
        return current == null ? new UnknownState(this, current) : current.getTrackState();
    }

    @Override
    public int getFoldersCount() {
        return listFolders.size();
    }

    @Override
    public File getRootFolder() {
        return rootFolder;
    }

    @Override
    public List<File> getListFolders() {
        return listFolders;
    }

    @Override
    public synchronized void jumpTrack(int jumps, SeekOption option) {
        int newIndex;
        if (option == SeekOption.NEXT) {
            newIndex = playerData.getTrackIndex() + jumps;
            if (newIndex >= listTracks.size())
                newIndex = 0;
        } else {
            newIndex = playerData.getTrackIndex() - jumps;
            if (newIndex < 0)
                newIndex = listTracks.size() - 1;
        }
        play(newIndex);
    }

    @Override
    public synchronized List<File> getListSoundFiles() {
        return listTracks.stream().filter(track -> track.getDataSourceAsFile() != null)
                .map(track -> (File) track.getDataSourceAsFile()).collect(Collectors.toList());
    }

    @Override
    public synchronized List<Track> getTracks() {
        return listTracks;
    }

    @Override
    public synchronized List<Artist> getArtists() {
        final List<Track> listTracks = getTracks();
        final Set<Artist> setArtists = new HashSet<>(listTracks.size() + 1);

        listTracks.parallelStream()
                .forEach(track -> {
                    final String artistName = track.getArtist() != null ? track.getArtist() : "Unknown";
                    synchronized (setArtists) {
                        Artist artist = setArtists.parallelStream()
                                .filter(art -> art.getName().equals(artistName))
                                .findFirst().orElse(null);
                        if (artist != null)
                            artist.addTrack(track);
                        else {
                            artist = new Artist(artistName);
                            artist.addTrack(track);
                            setArtists.add(artist);
                        }
                    }
                });
        final List<Artist> listArtists = new ArrayList<>(setArtists);
        listArtists.sort(Comparator.comparing(Artist::getName));
        return listArtists;
    }

    @Override
    public synchronized List<Album> getAlbums() {
        final List<Track> listTracks = getTracks();
        final Set<Album> setAlbums = new HashSet<>(listTracks.size() + 1);

        listTracks.parallelStream()
                .forEach(track -> {
                    final String albumName = track.getAlbum() != null ? track.getAlbum() : "Unknown";
                    synchronized (setAlbums) {
                        Album album = setAlbums.parallelStream()
                                .filter(alb -> alb.getName().equals(albumName))
                                .findFirst().orElse(null);
                        if (album != null)
                            album.addTrack(track);
                        else {
                            album = new Album(albumName);
                            album.addTrack(track);
                            setAlbums.add(album);
                        }
                    }
                });
        final List<Album> listAlbums = new ArrayList<>(setAlbums);
        listAlbums.sort(Comparator.comparing(Album::getName));
        return listAlbums;
    }

    @Override
    public synchronized void addPlayerListener(PlayerListener listener) {
        listListeners.add(listener);
    }

    @Override
    public synchronized List<PlayerListener> getListeners() {
        return listListeners;
    }

    @Override
    public synchronized void removePlayerListener(PlayerListener reference) {
        listListeners.removeIf(listener -> listener.equals(reference));
    }

    @Override
    public synchronized void removeAllListeners() {
        listListeners.clear();
    }

    @Override
    public PlayerInfo getInfo() {
        return new PlayerInfo(this);
    }

    @Override
    public synchronized Track getCurrent() {
        return current;
    }

    @Override
    public synchronized Track getNext() {
        final int trackIndex = playerData.getTrackIndex();
        final int songsCount = getSongsCount();
        final int nextIndex = trackIndex == -1 ? 0 : (trackIndex == songsCount - 1 ? 0 : trackIndex + 1);
        return listTracks.get(nextIndex);
    }

    @Override
    public synchronized Track getPrevious() {
        final int trackIndex = playerData.getTrackIndex();
        final int songsCount = getSongsCount();
        final int prevIndex = trackIndex == -1 ? songsCount - 1 : (trackIndex == 0 ? songsCount - 1 : trackIndex - 1);
        return listTracks.get(prevIndex);
    }

    @Override
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

    /*@Override
    public synchronized boolean isFinished() {
        return current != null && current.isFinished();
    }*/

    @Override
    public synchronized boolean isMute() {
        return playerData.isMute();
    }

    // ojo cuando se agrega musica de carpetas que estan fuera de rootFolder
    // puede ocasionar problemas
    @Override
    public synchronized void addMusic(Collection<File> soundCollection) {
        if (!soundCollection.isEmpty()) {
            soundCollection.forEach(this::loadTracks);
            waitForTracksLoading();
        }
    }

    @Override
    public synchronized void addMusic(File folderOrFile) {
        if (folderOrFile.isDirectory()) {
            if (rootFolder == null)
                rootFolder = folderOrFile;
            final boolean validSort = !hasSounds();
            loadTracks(folderOrFile);
            waitForTracksLoading();
            if (validSort)
                sortTracks();
        } else if (AudioUtil.isSupportedFile(folderOrFile)) {
            final Track track = Track.getTrack(folderOrFile, this);
            if (track != null) {
                listTracks.add(track);
                final File parent = folderOrFile.getParentFile();
                if (listFolders.parallelStream().noneMatch(parent::equals))
                    listFolders.add(parent);
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
            final File parentToFind;

            if (option == SeekOption.NEXT) {
                newFolderIndex = folderIndex + jumps;
                parentToFind = listFolders.get(newFolderIndex >= getFoldersCount()
                        ? 0 : newFolderIndex);
            } else {
                newFolderIndex = folderIndex - jumps;
                parentToFind = listFolders.get(newFolderIndex < 0
                        ? listFolders.size() - 1 : newFolderIndex);
            }
            changeTrack(findFirstIn(parentToFind.getPath()));
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

    private void play(TrackIndexed trackIndexed) {
        int index = trackIndexed.getIndex();
        Track track = trackIndexed.getTrack();

        if (current != null) {
            restartCurrent();
        }
        if (track != null) {
            current = track;
            startTrackThread();
            playerData.setTrackIndex(index);
            loadListenerMethod(ONPLAYED, current);
        }
    }

    @Override
    public void play(int index) {
        if (index > -1 && index < getSongsCount()) {
            final Track track = listTracks.get(index);
            play(new TrackIndexed(track, index));
        }
    }

    // Reproduce archivo de audio en la lista
    // (is alive)
    @Override
    public synchronized void play(File track) {
        Predicate<Track> filter = FilterUtil.getPlayByPathFilter(track.getPath());
        TrackIndexed trackIndexed = getTrackIndexedFromCondition(filter);

        if (trackIndexed == null) {
            if (AudioUtil.isSupportedFile(track)) {
                listTracks.add(Track.getTrack(track, this));
                if (!existsFolder(track.getParent()))
                    listFolders.add(track.getParentFile());
            }
        } else {
            playerData.setTrackIndex(playerData.getTrackIndex());
            if (current != null) {
                restartCurrent();
            }
            current = listTracks.get(playerData.getTrackIndex());
            startTrackThread();
            loadListenerMethod(ONPLAYED, current);
        }
    }

    @Override
    public synchronized void play(String trackName) {
        Predicate<Track> filter = FilterUtil.getPlayByNameFilter(trackName);
        TrackIndexed trackIndexed = getTrackIndexedFromCondition(filter);

        if (trackIndexed != null) {
            playerData.setTrackIndex(trackIndexed.getIndex());
            if (current != null) {
                restartCurrent();
            }
            current = trackIndexed.getTrack();
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

    //@Override
    public void reload() throws Exception {
        if (rootFolder != null) {
            final int currentIndex = playerData.getTrackIndex();
            listTracks.clear();
            listFolders.clear();
            setupTracksList();

            final int songCount = getSongsCount();
            playerData.setTrackIndex(songCount > currentIndex ? currentIndex : songCount - 1);
        }
    }

    /*@Override
    public synchronized void replace() {
        shutdown();
    }*/

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
    public synchronized float getVolume() {
        return current == null
                ? playerData.getVolume() : current.getVolume();
    }

    // 0-100
    @Override
    public synchronized void setVolume(float volume) {
        playerData.setVolume(volume);
        if (current != null)
            current.setVolume(volume);
    }

    @Override
    public synchronized void mute() {
        playerData.setMute(true);
        if (current != null)
            current.mute();
    }

    @Override
    public synchronized void unMute() {
        if (playerData.isVolumeZero())
            playerData.setVolume(100);
        else
            playerData.setMute(false);
        if (current != null)
            current.unMute();
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
        restartCurrent();
        playFolderSongs(path);
    }

    @Override
    public void playFolder(int folderIndex) {
        final int foldersCount = getFoldersCount();
        if (folderIndex >= foldersCount)
            folderIndex = foldersCount - 1;
        final int newIndex = seekToFolder(listFolders.get(folderIndex).getPath());
        changeTrack(newIndex);
    }

    @Override
    public synchronized void shutdown() {
        playerData.setOn(false);
        restartCurrent();
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
