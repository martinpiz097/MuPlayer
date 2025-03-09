package org.muplayer.audio.player;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.muplayer.audio.track.Track;
import org.muplayer.audio.track.TrackBuilder;
import org.muplayer.audio.track.state.TrackState;
import org.muplayer.audio.track.state.UnknownState;
import org.muplayer.exception.FormatNotSupportedException;
import org.muplayer.listener.PlayerListener;
import org.muplayer.model.*;
import org.muplayer.service.LogService;
import org.muplayer.service.impl.LogServiceImpl;
import org.muplayer.thread.*;
import org.muplayer.util.*;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.muplayer.listener.ListenerMethodName.*;
import static org.muplayer.model.SeekOption.NEXT;
import static org.muplayer.model.SeekOption.PREV;

@Log
public class MuPlayer extends Player {
    private volatile File rootFolder;
    private volatile Track current;

    private final List<Track> listTracks;
    private final List<File> listFolders;
    private final List<PlayerListener> listListeners;

    private final PlayerStatusData playerStatusData;
    private final TracksLoader tracksLoader;
    private final TrackBuilder trackBuilder;
    private final FilterUtil filterUtil;
    @Getter private final MuPlayerUtil muPlayerUtil;

    private final LogService losService;

    public MuPlayer() throws FileNotFoundException {
        this((File) null);
    }

    public MuPlayer(File rootFolder) throws FileNotFoundException {
        this.rootFolder = rootFolder;
        this.listTracks = CollectionUtil.newFastArrayList();
        this.listFolders = CollectionUtil.newMinimalFastArrayList();
        this.listListeners = CollectionUtil.newLinkedList();
        this.playerStatusData = new PlayerStatusData();
        this.tracksLoader = TracksLoader.getInstance();
        this.trackBuilder = new TrackBuilder();
        this.filterUtil = new FilterUtil();
        this.losService = new LogServiceImpl();
        this.muPlayerUtil = new MuPlayerUtil(listTracks, listFolders, listListeners, playerStatusData);

        setName("MuPlayer " + getId());

    }

    public MuPlayer(String folderPath) throws FileNotFoundException {
        this(new File(folderPath));
    }

    private void checkRootFolder() throws FileNotFoundException {
        if (rootFolder != null && rootFolder.exists()
                && filterUtil.getDirectoriesFilter().accept(rootFolder)) {
            setupTracksList();
        } else if (rootFolder == null) {
            losService.warningLog("To set music folder run this: smf ${music-folder-path}\n");
        } else {
            throw new FileNotFoundException(rootFolder.getPath());
        }
    }

    private void loadTracks(File folderToLoad) {
//        Files.find()
        tracksLoader.addTask(() -> {
            tracksLoader.addTask(() -> {
                final File[] fldDirs = folderToLoad.listFiles(filterUtil.getDirectoriesFilter());
                if (fldDirs != null && fldDirs.length > 0) {
                    CollectionUtil.streamOf(fldDirs, true)
                            .forEach(this::loadTracks);
                }
            });

            tracksLoader.addTask(() -> {
                final File[] fldAudioFiles = folderToLoad.listFiles(filterUtil.getAudioFileFilter());
                if (fldAudioFiles != null && fldAudioFiles.length > 0) {
                    synchronized (listFolders) {
                        listFolders.add(folderToLoad);
                    }

                    final List<Track> listTraksSync = Collections.synchronizedList(listTracks);
                    CollectionUtil.streamOf(fldAudioFiles, true)
                            .forEach(audioFile -> {
                                final TrackBuilder localTrackBuilder = new TrackBuilder();
                                final Track track;
                                try {
                                    track = localTrackBuilder.getTrack(audioFile, this);
                                    if (track != null) {
                                        listTraksSync.add(track);
                                    }
                                } catch (FormatNotSupportedException e) {
                                }
                            });


//                    CollectionUtil.streamOf(fldAudioFiles, true)
//                            .map(audioFile -> {
//                                final TrackBuilder localTrackBuilder = new TrackBuilder();
//                                final Track track;
//                                try {
//                                    track = localTrackBuilder.getTrack(audioFile, this);
//                                    return track;
//                                } catch (FormatNotSupportedException e) {
//                                    return null;
//                                }
//                            })
//                            .filter(Objects::nonNull)
//                            .sequential()
//                            .collect(Collectors.toCollection(() -> listTracks));
                }
            });
        });
    }

    private void setupTracksList() {
        loadTracks(rootFolder);
        muPlayerUtil.waitForTracksLoading();
        muPlayerUtil.sortTracks();
    }

    private void playFolderSongs(String fldPath) {
        final Predicate<Track> filter = filterUtil.getPlayFolderFilter(fldPath);
        final TrackIndexed trackIndexed = muPlayerUtil.getTrackIndexedFromCondition(filter);

        if (trackIndexed != null) {
            play(trackIndexed);
        }
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
        if (option == NEXT) {
            newIndex = playerStatusData.getTrackIndex() + jumps;
            if (newIndex >= listTracks.size()) {
                newIndex = 0;
            }
        } else {
            newIndex = playerStatusData.getTrackIndex() - jumps;
            if (newIndex < 0) {
                newIndex = listTracks.size() - 1;
            }
        }
        play(newIndex);
    }

    @Override
    public synchronized List<File> getListSoundFiles() {
        return listTracks.stream()
                .map(Track::getDataSource)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(CollectionUtil::newLinkedList));
    }

    @Override
    public synchronized List<Track> getTracks() {
        return listTracks;
    }

    @Override
    public synchronized List<Artist> getArtists() {
        final List<Track> trackList = getTracks();
        final Set<Artist> setArtists = new HashSet<>(trackList.size() + 1);

        trackList.parallelStream()
                .forEach(track -> {
                    final String artistName = track.getArtist() != null ? track.getArtist() : "Unknown";
                    synchronized (setArtists) {
                        Artist artist = setArtists.parallelStream()
                                .filter(art -> art.getName().equals(artistName))
                                .findFirst().orElse(null);
                        if (artist != null) {
                            artist.addTrack(track);
                        } else {
                            artist = new Artist(artistName);
                            artist.addTrack(track);
                            setArtists.add(artist);
                        }
                    }
                });
        final List<Artist> listArtists = CollectionUtil.newFastList(setArtists);
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
                        if (album != null) {
                            album.addTrack(track);
                        } else {
                            album = new Album(albumName);
                            album.addTrack(track);
                            setAlbums.add(album);
                        }
                    }
                });
        final List<Album> listAlbums = CollectionUtil.newFastList(setAlbums);
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
        return muPlayerUtil.getTrackBySeekOption(NEXT);
    }

    @Override
    public synchronized Track getPrevious() {
        return muPlayerUtil.getTrackBySeekOption(PREV);
    }

    @Override
    public int getSongsCount() {
        return muPlayerUtil.getSongsCount();
    }

    @Override
    public boolean hasSounds() {
        return !listTracks.isEmpty();
    }

    @Override
    public synchronized boolean isOn() {
        return playerStatusData.isOn();
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
    public synchronized boolean isMute() {
        return playerStatusData.isMute();
    }

    // ojo cuando se agrega musica de carpetas que estan fuera de rootFolder
    // puede ocasionar problemas
    @Override
    public synchronized void addMusic(Collection<File> soundCollection) {
        if (!soundCollection.isEmpty()) {
            soundCollection.forEach(this::loadTracks);
            muPlayerUtil.waitForTracksLoading();
        }
    }

    @Override
    public synchronized void addMusic(File folderOrFile) {
        if (folderOrFile.isDirectory()) {
            if (rootFolder == null) {
                rootFolder = folderOrFile;
            }
            final boolean validSort = !hasSounds();
            loadTracks(folderOrFile);
            muPlayerUtil.waitForTracksLoading();
            if (validSort) {
                muPlayerUtil.sortTracks();
            }
        } else if (audioUtil.isSupportedFile(folderOrFile)) {
            final Track track = trackBuilder.getTrack(folderOrFile, this);
            if (track != null) {
                listTracks.add(track);
                final File parent = folderOrFile.getParentFile();
                if (listFolders.parallelStream().noneMatch(parent::equals)) {
                    listFolders.add(parent);
                }
            }
        }
    }

    @Override
    public synchronized void seekFolder(SeekOption param) {
        seekFolder(param, 1);
    }

    @Override
    public synchronized void seekFolder(SeekOption option, int jumps) {
        final int folderIndex = muPlayerUtil.getFolderIndex(current);
        if (folderIndex != -1) {
            final int newFolderIndex;
            final File parentToFind;

            if (option == NEXT) {
                newFolderIndex = folderIndex + jumps;
                parentToFind = listFolders.get(newFolderIndex >= getFoldersCount()
                        ? 0 : newFolderIndex);
            } else {
                newFolderIndex = folderIndex - jumps;
                parentToFind = listFolders.get(newFolderIndex < 0
                        ? listFolders.size() - 1 : newFolderIndex);
            }

            final TrackIndexed firstIn = muPlayerUtil.findFirstIn(parentToFind.getPath());
            muPlayerUtil.changeTrack(firstIn, current);
        }
    }

    @Override
    public synchronized void play() {
        if (!isAlive()) {
            start();
        } else if (current != null) {
            current.play();
            muPlayerUtil.loadListenerMethod(ON_PLAYED, current);
        }
    }

    private void play(TrackIndexed trackIndexed) {
        int index = trackIndexed.getIndex();
        Track track = trackIndexed.getTrack();

        if (current != null) {
            muPlayerUtil.restartCurrent(current);
        }
        if (track != null) {
            current = track;
            muPlayerUtil.startTrackThread(current);
            playerStatusData.setTrackIndex(index);
            muPlayerUtil.loadListenerMethod(ON_PLAYED, current);
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
        Predicate<Track> filter = filterUtil.getPlayByPathFilter(track.getPath());
        TrackIndexed trackIndexed = muPlayerUtil.getTrackIndexedFromCondition(filter);

        if (trackIndexed == null) {
            if (audioUtil.isSupportedFile(track)) {
                listTracks.add(trackBuilder.getTrack(track, this));
                if (!CollectionUtil.existsFolder(listFolders, track.getParent())) {
                    listFolders.add(track.getParentFile());
                }
            }
        } else {
            playerStatusData.setTrackIndex(playerStatusData.getTrackIndex());
            if (current != null) {
                muPlayerUtil.restartCurrent(current);
            }
            current = listTracks.get(playerStatusData.getTrackIndex());
            muPlayerUtil.startTrackThread(current);
            muPlayerUtil.loadListenerMethod(ON_PLAYED, current);
        }
    }

    @Override
    public synchronized void play(String trackName) {
        Predicate<Track> filter = filterUtil.getPlayByNameFilter(trackName);
        TrackIndexed trackIndexed = muPlayerUtil.getTrackIndexedFromCondition(filter);

        if (trackIndexed != null) {
            playerStatusData.setTrackIndex(trackIndexed.getIndex());
            if (current != null) {
                muPlayerUtil.restartCurrent(current);
            }
            current = trackIndexed.getTrack();
            muPlayerUtil.startTrackThread(current);
            muPlayerUtil.loadListenerMethod(ON_PLAYED, current);
        }
    }

    @Override
    public synchronized void pause() {
        if (current != null) {
            current.pause();
            muPlayerUtil.loadListenerMethod(ON_PAUSED, current);
        }
    }

    @Override
    public synchronized void resumeTrack() {
        if (current != null) {
            current.resumeTrack();
            muPlayerUtil.loadListenerMethod(ON_RESUMED, current);
        }
    }

    @Override
    public synchronized void stopTrack() {
        if (current != null) {
            current.stopTrack();
            muPlayerUtil.loadListenerMethod(ON_STOPPED, current);
        }
    }

    //@Override
    public void reload() throws Exception {
        if (rootFolder != null) {
            final int currentIndex = playerStatusData.getTrackIndex();
            listTracks.clear();
            listFolders.clear();
            setupTracksList();

            final int songCount = getSongsCount();
            playerStatusData.setTrackIndex(songCount > currentIndex ? currentIndex : songCount - 1);
        }
    }

    @Override
    public synchronized void seek(double seconds) {
        if (current != null) {
            try {
                current.seek(seconds);
                muPlayerUtil.loadListenerMethod(ON_GO_TO_SECOND, current);
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
                muPlayerUtil.loadListenerMethod(ON_GO_TO_SECOND, current);
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized float getVolume() {
        return current == null
                ? playerStatusData.getVolume() : current.getVolume();
    }

    // 0-100
    @Override
    public synchronized void setVolume(float volume) {
        playerStatusData.setVolume(volume);
        if (current != null) {
            current.setVolume(volume);
        }
    }

    @Override
    public synchronized void mute() {
        playerStatusData.setMute(true);
        if (current != null) {
            current.mute();
        }
    }

    @Override
    public synchronized void unMute() {
        if (playerStatusData.isVolumeZero()) {
            playerStatusData.setVolume(100);
        } else {
            playerStatusData.setMute(false);
        }
        if (current != null) {
            current.unMute();
        }
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
        current = muPlayerUtil.changeTrack(NEXT, current);
    }

    @Override
    public synchronized void playPrevious() {
        current = muPlayerUtil.changeTrack(PREV, current);
    }

    @Override
    public void playFolder(String path) {
        muPlayerUtil.restartCurrent(current);
        playFolderSongs(path);
    }

    @Override
    public void playFolder(int folderIndex) {
        final int foldersCount = getFoldersCount();
        if (folderIndex >= foldersCount) {
            folderIndex = foldersCount - 1;
        }
        final int newIndex = muPlayerUtil.seekToFolder(listFolders.get(folderIndex).getPath());
        muPlayerUtil.changeTrack(newIndex, current);
    }

    @Override
    public synchronized void shutdown() {
        playerStatusData.setOn(false);
        muPlayerUtil.restartCurrent(current);
        this.interrupt();
        muPlayerUtil.loadListenerMethod(ON_SHUTDOWN, null);
    }

    @SneakyThrows(Exception.class)
    @Override
    public synchronized void run() {
        checkRootFolder();
        playerStatusData.setOn(true);
        muPlayerUtil.loadListenerMethod(ON_STARTED, null);
        muPlayerUtil.waitForSongs();
        playNext();
        ThreadUtil.freezeThread(this);
    }

}
