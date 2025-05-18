package cl.estencia.labs.muplayer.audio.player;

import cl.estencia.labs.aucom.core.util.AudioSystemManager;
import cl.estencia.labs.muplayer.audio.interfaces.SystemAudioController;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.state.TrackStateName;
import cl.estencia.labs.muplayer.event.listener.PlayerListener;
import cl.estencia.labs.muplayer.event.listener.TrackStateListener;
import cl.estencia.labs.muplayer.event.notifier.internal.PlayerInternalEventNotifier;
import cl.estencia.labs.muplayer.event.notifier.user.PlayerUserEventNotifier;
import cl.estencia.labs.muplayer.audio.model.Album;
import cl.estencia.labs.muplayer.audio.model.Artist;
import cl.estencia.labs.muplayer.core.common.enums.SeekOption;
import cl.estencia.labs.muplayer.audio.model.TrackIndexed;
import cl.estencia.labs.muplayer.core.thread.ThreadUtil;
import cl.estencia.labs.muplayer.audio.util.AudioFormatUtil;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import cl.estencia.labs.muplayer.audio.util.FilterUtil;
import cl.estencia.labs.muplayer.audio.util.MuPlayerUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cl.estencia.labs.muplayer.event.listener.PlayerEventType.*;
import static cl.estencia.labs.muplayer.core.common.enums.SeekOption.NEXT;
import static cl.estencia.labs.muplayer.core.common.enums.SeekOption.PREV;

@Log
public class MuPlayer extends Player implements SystemAudioController {
    private final File rootFolder;
    private final AtomicReference<Track> currentTrack;

    private final List<Track> listTracks;
    private final List<File> listFolders;

    private final List<TrackStateListener> listInternalTrackListeners;
    @Getter private final List<TrackStateListener> listTrackUserListeners;

    private final PlayerStatusData playerStatusData;
    private final FilterUtil filterUtil;
    @Getter private final MuPlayerUtil muPlayerUtil;
    private final AudioFormatUtil audioFormatUtil;
    private final AudioSystemManager audioSystemManager;

    private final PlayerInternalEventNotifier internalEventNotifier;
    private final PlayerUserEventNotifier userEventNotifier;

    public MuPlayer() throws FileNotFoundException {
        this((File) null);
    }

    public MuPlayer(File rootFolder) throws FileNotFoundException {
        this.rootFolder = rootFolder;
        this.currentTrack = new AtomicReference<>();
        this.listTracks = CollectionUtil.newFastArrayList();
        this.listFolders = CollectionUtil.newMinimalFastArrayList();
        this.listInternalTrackListeners = CollectionUtil.newMinimalFastArrayList();
        this.listTrackUserListeners = CollectionUtil.newMinimalFastArrayList();
        this.playerStatusData = new PlayerStatusData();
        this.filterUtil = new FilterUtil();
        this.internalEventNotifier = new PlayerInternalEventNotifier();
        this.userEventNotifier = new PlayerUserEventNotifier();
        this.muPlayerUtil = new MuPlayerUtil(this, playerStatusData,
                internalEventNotifier, listInternalTrackListeners);
        this.audioFormatUtil = new AudioFormatUtil();
        this.audioSystemManager = new AudioSystemManager();

        setName("MuPlayer " + getId());
    }

    public MuPlayer(String folderPath) throws FileNotFoundException {
        this(new File(folderPath));
    }

   

    private void loadTracks(File folderToLoad) {
//        Files.find()
        try (Stream<Path> folderPaths = Files.walk(
                Path.of(folderToLoad.toURI())).parallel()) {
            if (hasSounds()) {
                listTracks.clear();
                listFolders.clear();
            }

            folderPaths
                    .filter(muPlayerUtil::hasAudioFormatExtension)
                    .map(path -> muPlayerUtil.
                            loadTrackFromFile(path.toFile()))
                    .filter(Objects::nonNull)
                    .sorted(MuPlayerUtil.TRACKS_SORT_COMPARATOR)
                    .sequential()
                    .forEach(listTracks::add);

            listTracks.parallelStream()
                    .map(track -> track.getDataSource().getParentFile())
                    .distinct()
                    .sorted(MuPlayerUtil.FOLDERS_COMPARATOR)
                    .sequential()
                    .forEach(listFolders::add);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void setupTracksList() {
        loadTracks(rootFolder);
        internalEventNotifier.sendEvent(muPlayerUtil.createPlayerEvent(UPDATED_TRACK_LIST));
    }

    private void playFolderSongs(String fldPath) {
        final Predicate<Track> filter = filterUtil.getPlayFolderFilter(fldPath);
        final TrackIndexed trackIndexed = muPlayerUtil.getTrackIndexedFromCondition(filter);

        if (trackIndexed != null) {
            play(trackIndexed.getIndex());
        }
    }

    private void configureNotifiers() {
        this.internalEventNotifier.addListener(muPlayerUtil.createDefaultListener());
        this.internalEventNotifier.start();
        this.userEventNotifier.start();
    }

    @Override
    public TrackStateName getCurrentTrackState() {
        return currentTrack.get() != null
                ? currentTrack.get().getStateName() :
                TrackStateName.UNKNOWN;
    }

    @Override
    public PlayerStatusData getPlayerStatusData() {
        return playerStatusData;
    }

    @Override
    public ReadableStatusData getStatusData() {
        return new ReadableStatusData(
                playerStatusData.getCurrentTrackIndex(),
                playerStatusData.getNewTrackIndex(),
                playerStatusData.getVolume(),
                playerStatusData.isOn(),
                playerStatusData.isMute()
        );
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
            newIndex = playerStatusData.getCurrentTrackIndex() + jumps;
            if (newIndex >= listTracks.size()) {
                newIndex = 0;
            }
        } else {
            newIndex = playerStatusData.getCurrentTrackIndex() - jumps;
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
        userEventNotifier.addListener(listener);
    }

    @Override
    public void addTrackListener(TrackStateListener trackStateListener) {
        listTrackUserListeners.add(trackStateListener);
    }

    @Override
    public synchronized List<PlayerListener> getPlayerListeners() {
        return userEventNotifier.getAllListeners();
    }

    @Override
    public List<TrackStateListener> getTrackListeners() {
        return listTrackUserListeners;
    }

    @Override
    public synchronized void removePlayerListener(PlayerListener playerListener) {
        userEventNotifier.removeListener(playerListener);
    }

    @Override
    public void removeTrackListener(TrackStateListener trackStateListener) {
        listInternalTrackListeners.remove(trackStateListener);
    }

    @Override
    public synchronized void removeAllPlayerListeners() {
        userEventNotifier.removeAllListeners();
    }

    @Override
    public void removeAllTrackListeners() {
        listTrackUserListeners.clear();
    }

    @Override
    public PlayerInfo getInfo() {
        return new PlayerInfo(this);
    }

    @Override
    public AtomicReference<Track> getCurrentTrack() {
        return currentTrack;
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
        return listTracks.size();
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
        return currentTrack.get() != null && currentTrack.get().isPlaying();
    }

    @Override
    public synchronized boolean isPaused() {
        return currentTrack.get() != null && currentTrack.get().isPaused();
    }

    @Override
    public synchronized boolean isStopped() {
        return currentTrack.get() != null && currentTrack.get().isStopped();
    }

    @Override
    public synchronized boolean isMute() {
        return playerStatusData.isMute();
    }

    // ojo cuando se agrega musica de carpetas que estan fuera de rootFolder
    // puede ocasionar problemas
    @Override
    public synchronized void addMusic(Collection<File> soundCollection) {
//        if (!soundCollection.isEmpty()) {
//            soundCollection.forEach(this::setupTracksList);
//            muPlayerUtil.waitForTracksLoading();
//        }
    }

    @Override
    public synchronized void addMusic(File folderOrFile) {
//        if (folderOrFile.isDirectory()) {
//            if (rootFolder == null) {
//                rootFolder = folderOrFile;
//            }
//            final boolean validSort = !hasSounds();
//            setupTracksList(folderOrFile);
//            muPlayerUtil.waitForTracksLoading();
//            if (validSort) {
//                muPlayerUtil.sortTracks();
//            }
//        } else if (audioSupportUtil.isSupportedFile(folderOrFile)) {
//            final Track track = trackBuilder.getTrack(folderOrFile, this);
//            if (track != null) {
//                listTracks.add(track);
//                final File parent = folderOrFile.getParentFile();
//                if (listFolders.parallelStream().noneMatch(parent::equals)) {
//                    listFolders.add(parent);
//                }
//            }
//        }
    }

    @Override
    public synchronized void seekFolder(SeekOption param) {
        seekFolder(param, 1);
    }

    @Override
    public synchronized void seekFolder(SeekOption option, int jumps) {
        final int folderIndex = muPlayerUtil.getFolderIndex(currentTrack.get());
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
            play(firstIn.getIndex());
        }
    }

    @Override
    public synchronized void play() {
        if (!isAlive()) {
            start();
        } else if (currentTrack.get() != null) {
            currentTrack.get().play();
        }
    }

    @Override
    public void play(int index) {
        if (index < 0 || index >= getSongsCount()) {
            return;
        }

        if (currentTrack.get() != null) {
            currentTrack.get().finish();
        } else {
            internalEventNotifier.sendEvent(
                    muPlayerUtil.createPlayerEvent(CHANGED_CURRENT_TRACK));
        }
    }

    // Reproduce archivo de audio en la lista
    // (is alive)
    // TODO REVISAR
    @Override
    public synchronized void play(File trackFile) {
        Predicate<Track> filter = filterUtil.getTrackFilterByPath(trackFile.getPath());
        TrackIndexed trackIndexed = muPlayerUtil.getTrackIndexedFromCondition(filter);

        if (trackIndexed == null) {
            Track newTrack = muPlayerUtil.loadTrackFromFile(trackFile);
            listTracks.add(newTrack);
            if (!CollectionUtil.existsFolder(listFolders, trackFile.getParent())) {
                listFolders.add(trackFile.getParentFile());
            }

            internalEventNotifier.sendEvent(muPlayerUtil.createPlayerEvent(UPDATED_TRACK_LIST));
            trackIndexed = new TrackIndexed(newTrack, getSongsCount());
        }

        play(trackIndexed.getIndex());
    }

    @Override
    public synchronized void play(String trackName) {
        Predicate<Track> filter = filterUtil.getTrackFilterByName(trackName);
        TrackIndexed trackIndexed = muPlayerUtil.getTrackIndexedFromCondition(filter);

        if (trackIndexed != null) {
            play(trackIndexed.getIndex());
        }
    }

    @Override
    public synchronized void pause() {
        if (currentTrack.get() != null) {
            currentTrack.get().pause();
        }
    }

    @Override
    public synchronized void resumeTrack() {
        if (currentTrack.get() != null) {
            currentTrack.get().resumeTrack();
        }
    }

    @Override
    public synchronized void stopTrack() {
        if (currentTrack.get() != null) {
            currentTrack.get().stopTrack();
        }
    }

    @Override
    // TODO revisar
    public void reload() throws Exception {
        if (rootFolder != null) {
            final int currentIndex = playerStatusData.getCurrentTrackIndex();
            listTracks.clear();
            listFolders.clear();
            setupTracksList();

            final int songCount = getSongsCount();
            playerStatusData.setCurrentTrackIndex(songCount > currentIndex ? currentIndex : songCount - 1);
        }
    }

    // SeekedState o monitorear mejor el Playing?
    @Override
    public synchronized void seek(double seconds) {
        if (currentTrack.get() != null) {
            try {
                currentTrack.get().seek(seconds);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void gotoSecond(double second) {
        if (currentTrack.get() != null) {
            try {
                currentTrack.get().gotoSecond(second);
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized float getVolume() {
        return currentTrack.get() == null
                ? playerStatusData.getVolume() : currentTrack.get().getVolume();
    }

    // 0-100
    @Override
    public synchronized void setVolume(float volume) {
        playerStatusData.setVolume(volume);
        if (currentTrack.get() != null) {
            currentTrack.get().setVolume(volume);
        }
    }

    @Override
    public synchronized void mute() {
        playerStatusData.setMute(true);
        if (currentTrack.get() != null) {
            currentTrack.get().mute();
        }
    }

    @Override
    public synchronized void unMute() {
        if (playerStatusData.isVolumeZero()) {
            playerStatusData.setVolume(100);
        } else {
            playerStatusData.setMute(false);
        }
        if (currentTrack.get() != null) {
            currentTrack.get().unMute();
        }
    }

    @Override
    public double getProgress() {
        return currentTrack.get() == null ? 0 : currentTrack.get().getProgress();
    }

    @Override
    public long getDuration() {
        return listTracks.stream().map(Track::getDuration)
                .reduce(Long::sum).orElse(0L);
    }

    @Override
    public synchronized void playNext() {
        int nextIndex = audioFormatUtil.getIndexFromOption(NEXT, playerStatusData, getSongsCount());
        play(nextIndex);
    }

    @Override
    public synchronized void playPrevious() {
        int prevIndex = audioFormatUtil.getIndexFromOption(PREV, playerStatusData, getSongsCount());
        play(prevIndex);
    }

    @Override
    public void playFolder(String path) {
        playFolderSongs(path);
    }

    @Override
    public void playFolder(int folderIndex) {
        final int foldersCount = getFoldersCount();
        if (folderIndex >= foldersCount) {
            folderIndex = foldersCount - 1;
        }
        final int newIndex = muPlayerUtil.seekToFolder(listFolders.get(folderIndex).getPath());
        play(newIndex);
    }

    @Override
    public synchronized void shutdown() {
        playerStatusData.setOn(false);
        internalEventNotifier.sendEvent(muPlayerUtil.createPlayerEvent(SHUTDOWN));
        this.interrupt();
    }

    @SneakyThrows(Exception.class)
    @Override
    public synchronized void run() {
        internalEventNotifier.sendEvent(muPlayerUtil.createPlayerEvent(PRE_START));
        configureNotifiers();
        internalEventNotifier.sendEvent(muPlayerUtil.createPlayerEvent(START));
        ThreadUtil.freezeThread(this);
    }

    @Override
    public float getSystemVolume() {
        return audioSystemManager.getFormattedMasterVolume();
    }

    @Override
    public void setSystemVolume(float volume) {
        audioSystemManager.setFormattedMasterVolume(volume);
    }

}
