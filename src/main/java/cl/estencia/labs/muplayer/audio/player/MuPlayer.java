package cl.estencia.labs.muplayer.audio.player;

import cl.estencia.labs.muplayer.listener.PlayerEventType;
import cl.estencia.labs.muplayer.listener.PlayerListener;
import cl.estencia.labs.muplayer.audio.track.StandardTrackFactory;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.TrackFactory;
import cl.estencia.labs.muplayer.audio.track.state.TrackStateName;
import cl.estencia.labs.muplayer.listener.event.PlayerEvent;
import cl.estencia.labs.muplayer.listener.notifier.PlayerEventNotifier;
import cl.estencia.labs.muplayer.model.Album;
import cl.estencia.labs.muplayer.model.Artist;
import cl.estencia.labs.muplayer.model.SeekOption;
import cl.estencia.labs.muplayer.model.TrackIndexed;
import cl.estencia.labs.muplayer.service.LogService;
import cl.estencia.labs.muplayer.service.impl.LogServiceImpl;
import cl.estencia.labs.muplayer.thread.ThreadUtil;
import cl.estencia.labs.muplayer.util.CollectionUtil;
import cl.estencia.labs.muplayer.util.FilterUtil;
import cl.estencia.labs.muplayer.util.MuPlayerUtil;
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

import static cl.estencia.labs.muplayer.listener.PlayerEventType.*;
import static cl.estencia.labs.muplayer.model.SeekOption.NEXT;
import static cl.estencia.labs.muplayer.model.SeekOption.PREV;

@Log
public class MuPlayer extends Player {
    private final AtomicReference<File> rootFolder;
    private final AtomicReference<Track> current;

    private final List<Track> listTracks;
    private final List<File> listFolders;

    private final PlayerStatusData playerStatusData;
    private final FilterUtil filterUtil;
    @Getter private final MuPlayerUtil muPlayerUtil;

    private final PlayerEventNotifier playerEventNotifier;
    private final LogService losService;

    public MuPlayer() throws FileNotFoundException {
        this((File) null);
    }

    public MuPlayer(File rootFolder) throws FileNotFoundException {
        this.rootFolder = new AtomicReference<>(rootFolder);
        this.current = new AtomicReference<>();
        this.listTracks = CollectionUtil.newFastArrayList();
        this.listFolders = CollectionUtil.newMinimalFastArrayList();
        this.playerStatusData = new PlayerStatusData();
        this.filterUtil = new FilterUtil();
        this.losService = new LogServiceImpl();
        this.playerEventNotifier = new PlayerEventNotifier();
        this.muPlayerUtil = new MuPlayerUtil(this, playerStatusData, playerEventNotifier);

        setName("MuPlayer " + getId());
    }

    public MuPlayer(String folderPath) throws FileNotFoundException {
        this(new File(folderPath));
    }

    private void checkRootFolder() throws FileNotFoundException {
        if (rootFolder.get() != null && rootFolder.get().exists()
                && filterUtil.getDirectoriesFilter().accept(rootFolder.get())) {
            setupTracksList();
        } else if (rootFolder.get() == null) {
            losService.warningLog("To set music folder run this: smf ${music-folder-path}\n");
        } else {
            throw new FileNotFoundException(rootFolder.get().getPath());
        }
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
        loadTracks(rootFolder.get());
        playerEventNotifier.sendEvent(muPlayerUtil.createPlayerEvent(UPDATED_TRACK_LIST));
    }

    private void playFolderSongs(String fldPath) {
        final Predicate<Track> filter = filterUtil.getPlayFolderFilter(fldPath);
        final TrackIndexed trackIndexed = muPlayerUtil.getTrackIndexedFromCondition(filter);

        if (trackIndexed != null) {
            play(trackIndexed.getIndex());
        }
    }

    @Override
    public TrackStateName getCurrentTrackState() {
        return current.get() != null ? current.get().getStateName() : TrackStateName.UNKNOWN;
    }

    @Override
    public int getFoldersCount() {
        return listFolders.size();
    }

    @Override
    public File getRootFolder() {
        return rootFolder.get();
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
        playerEventNotifier.addUserListener(listener);
    }

    @Override
    public synchronized List<PlayerListener> getListeners() {
        return playerEventNotifier.getListUserListeners();
    }

    @Override
    public synchronized void removePlayerListener(PlayerListener playerListener) {
        playerEventNotifier.removeUserListener(playerListener);
    }

    @Override
    public synchronized void removeAllListeners() {
        playerEventNotifier.removeAllUserListeners();
    }

    @Override
    public PlayerInfo getInfo() {
        return new PlayerInfo(this);
    }

    @Override
    public synchronized AtomicReference<Track> getCurrent() {
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
        return current.get() != null && current.get().isPlaying();
    }

    @Override
    public synchronized boolean isPaused() {
        return current.get() != null && current.get().isPaused();
    }

    @Override
    public synchronized boolean isStopped() {
        return current.get() != null && current.get().isStopped();
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
//            if (rootFolder.get() == null) {
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
        final int folderIndex = muPlayerUtil.getFolderIndex(current.get());
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
        } else if (current.get() != null) {
            current.get().play();
        }
    }

    @Override
    public void play(int index) {
        if (index < 0 || index >= getSongsCount()) {
            return;
        }

        if (current.get() != null) {
            current.get().finish();
        } else {
            muPlayerUtil.playTrackByNewIndex();
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

            playerEventNotifier.sendEvent(muPlayerUtil.createPlayerEvent(UPDATED_TRACK_LIST));
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
        if (current.get() != null) {
            current.get().pause();
        }
    }

    @Override
    public synchronized void resumeTrack() {
        if (current.get() != null) {
            current.get().resumeTrack();
        }
    }

    @Override
    public synchronized void stopTrack() {
        if (current.get() != null) {
            current.get().stopTrack();
        }
    }

    @Override
    // TODO revisar
    public void reload() throws Exception {
        if (rootFolder.get() != null) {
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
        if (current.get() != null) {
            try {
                current.get().seek(seconds);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void gotoSecond(double second) {
        if (current.get() != null) {
            try {
                current.get().gotoSecond(second);
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized float getVolume() {
        return current.get() == null
                ? playerStatusData.getVolume() : current.get().getVolume();
    }

    // 0-100
    @Override
    public synchronized void setVolume(float volume) {
        playerStatusData.setVolume(volume);
        if (current.get() != null) {
            current.get().setVolume(volume);
        }
    }

    @Override
    public synchronized void mute() {
        playerStatusData.setMute(true);
        if (current.get() != null) {
            current.get().mute();
        }
    }

    @Override
    public synchronized void unMute() {
        if (playerStatusData.isVolumeZero()) {
            playerStatusData.setVolume(100);
        } else {
            playerStatusData.setMute(false);
        }
        if (current.get() != null) {
            current.get().unMute();
        }
    }

    @Override
    public double getProgress() {
        return current.get() == null ? 0 : current.get().getProgress();
    }

    @Override
    public long getDuration() {
        return listTracks.stream().map(Track::getDuration)
                .reduce(Long::sum).orElse(0L);
    }

    @Override
    public synchronized void playNext() {
        int nextIndex = muPlayerUtil.getIndexFromOption(NEXT);
        play(nextIndex);
    }

    @Override
    public synchronized void playPrevious() {
        int prevIndex = muPlayerUtil.getIndexFromOption(PREV);
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
        playerEventNotifier.sendEvent(muPlayerUtil.createPlayerEvent(SHUTDOWN));
        this.interrupt();
    }

    @SneakyThrows(Exception.class)
    @Override
    public synchronized void run() {
        playerEventNotifier.sendEvent(muPlayerUtil.createPlayerEvent(PRE_START));

        checkRootFolder();
        playerStatusData.setOn(true);
        muPlayerUtil.waitForSongs();

        play(0);
        playerEventNotifier.sendEvent(muPlayerUtil.createPlayerEvent(START));
        ThreadUtil.freezeThread(this);
    }

}
