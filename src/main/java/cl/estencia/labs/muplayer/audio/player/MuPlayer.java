package cl.estencia.labs.muplayer.audio.player;

import cl.estencia.labs.ebot.bus.exception.BusException;
import cl.estencia.labs.ebot.bus.model.message.Message;
import cl.estencia.labs.ebot.bus.model.pubsub.sub.MessageListener;
import cl.estencia.labs.ebot.utils.threads.Interruptor;
import cl.estencia.labs.muplayer.audio.common.enums.SeekOption;
import cl.estencia.labs.muplayer.audio.interfaces.SystemVolumeController;
import cl.estencia.labs.muplayer.audio.model.Album;
import cl.estencia.labs.muplayer.audio.model.Artist;
import cl.estencia.labs.muplayer.audio.model.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.model.TrackIndexed;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.TracksDirectory;
import cl.estencia.labs.muplayer.audio.track.state.TrackStateName;
import cl.estencia.labs.muplayer.audio.util.AudioFileUtil;
import cl.estencia.labs.muplayer.audio.util.MprisUtil;
import cl.estencia.labs.muplayer.audio.util.MuPlayerUtil;
import cl.estencia.labs.muplayer.core.aucom.util.AudioSystemManager;
import cl.estencia.labs.muplayer.core.bus.listener.PlayerResponseListener;
import cl.estencia.labs.muplayer.core.bus.message.PlayerEventTopics;
import cl.estencia.labs.muplayer.core.bus.model.PlayerInfo;
import cl.estencia.labs.muplayer.core.bus.model.SkipData;
import cl.estencia.labs.muplayer.core.cache.CacheVar;
import cl.estencia.labs.muplayer.core.thread.ThreadUtil;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import cl.estencia.labs.muplayer.core.util.FilterUtil;
import cl.estencia.labs.muplayer.core.util.NumberUtil;
import cl.estencia.labs.muplayer.unix.dbus.mpris.Mpris;
import cl.estencia.labs.muplayer.unix.dbus.mpris.common.PlaybackStatus;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.dbus.exceptions.DBusException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cl.estencia.labs.muplayer.audio.common.constants.PlayerConstants.PLAYER_BUS;
import static cl.estencia.labs.muplayer.audio.common.enums.SeekOption.NEXT;
import static cl.estencia.labs.muplayer.audio.common.enums.SeekOption.PREV;
import static cl.estencia.labs.muplayer.core.aucom.util.AudioDecodingUtil.DEFAULT_VOLUME;
import static cl.estencia.labs.muplayer.core.bus.message.PlayerEventTopics.*;
import static cl.estencia.labs.muplayer.core.cache.CacheManager.CACHE;
import static cl.estencia.labs.muplayer.unix.dbus.mpris.common.MprisConstants.BUS_NAME;

@Slf4j
public class MuPlayer extends MusicPlayer implements SystemVolumeController {
    private final File rootFolder;
    private final AtomicReference<Track> currentTrack;

    private final List<Track> listTracks;
    private final List<File> listFolders;

    private final Mpris mpris;

    private final PlayerStatusData playerStatusData;
    private final MuPlayerUtil muPlayerUtil;
    private final MprisUtil mprisUtil;
    private final AudioSystemManager audioSystemManager;

    private final Interruptor interruptor;

    public MuPlayer() throws FileNotFoundException, DBusException {
        this((File) null);
    }

    public MuPlayer(String folderPath) throws FileNotFoundException, DBusException {
        this(new File(folderPath));
    }

    public MuPlayer(File rootFolder) throws FileNotFoundException, DBusException {
        this(rootFolder, new Mpris(BUS_NAME));
    }

    public MuPlayer(File rootFolder, Mpris mpris) {
        this.rootFolder = rootFolder;
        this.currentTrack = new AtomicReference<>();
        this.listTracks = CollectionUtil.newBigList();
        this.listFolders = CollectionUtil.newList();
        this.mpris = mpris;
        this.playerStatusData = new PlayerStatusData();
        this.muPlayerUtil = new MuPlayerUtil(this, playerStatusData);
        this.mprisUtil = new MprisUtil(mpris);
        this.audioSystemManager = new AudioSystemManager();
        this.interruptor = Interruptor.manual(this);

        setName(getClass().getSimpleName() + threadId());
        configureListeners();
    }

    private void loadTracks(File folderToLoad) {
//        long ti = System.currentTimeMillis();

        try (Stream<Path> paths = Files.walk(folderToLoad.toPath());
             ExecutorService tracksLoadExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            // TODO si cambio a map, este metodo debe modificarse
            if (hasSounds()) {
                muPlayerUtil.killActiveTracks();
                muPlayerUtil.clearLists();
            }

            List<Future<TracksDirectory>> tasks = CollectionUtil.newList();
            LongAdder tasksCounter = new LongAdder();
            paths.parallel()
                    .filter(path -> path.toFile().exists()
                            && path.toFile().isDirectory())
                    .map(path -> tracksLoadExecutor.submit(() -> {
                        TracksDirectory tracksDirectory = new TracksDirectory(
                                path.toFile(), muPlayerUtil.createTracksSortComparator());

                        tracksDirectory.scanDirectory();
                        tasksCounter.increment();
                        return tracksDirectory;
                    }))
                    .forEachOrdered(tasks::add);

            while (tasksCounter.sum() < tasks.size()) {
                LockSupport.parkNanos(Duration.ofNanos(1).toNanos());
            }

            tasks.parallelStream()
                    .map(ThreadUtil::getTaskValueOrNull)
                    .filter(directory -> directory != null && directory.hasTracks())
                    .sorted(Comparator.comparing(TracksDirectory::getPath))
                    .forEachOrdered(tracksDirectory -> {
                        listTracks.addAll(tracksDirectory.getTracks());
                        listFolders.add(tracksDirectory.getFolder());

                        muPlayerUtil.sendLoadingInfoEvent(listTracks.size());
                    });

            tracksLoadExecutor.shutdown();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

//        long tf = System.currentTimeMillis();
//        infoLine("diff: " + (new DecimalFormat("#0.000").format(((double) (tf - ti)) / 1000)));
//        if (tf > ti) {
//            System.exit(0);
//        }
    }

    @Override
    protected void configureListeners() {
        try {
            addListener(START, message -> {
                if (!isAlive()) {
                    start();
                }
            });

            addListener(RELOAD, message -> reload());
            addListener(PLAY_NEXT, message -> playNext());
            addListener(PLAY_PREVIOUS, message -> playPrevious());
            addListener(PLAY_INDEX, message -> {
                Integer index = message.getData(Integer.class);
                if (index == null) {
                    return;
                }

                play(index);
            });

            addListener(PLAY_FOLDER, message -> {
                Integer index = message.getData(Integer.class);
                if (index == null) {
                    return;
                }

                playFolder(index);
            });

            addListener(PLAY, message -> play());
            addListener(SHUTDOWN, message -> shutdown());
            addListener(PAUSE, message -> pause());
            addListener(RESUME, message -> resumeTrack());
            addListener(STOP, message -> stopTrack());

            addListener(SEEK_SECONDS, message -> {
                Integer seconds = message.getData(Integer.class);
                if (seconds == null) {
                    return;
                }

                seek(seconds);
            });

            addListener(SKIP_TRACKS, message -> {
                SkipData skipData = message.getData(SkipData.class);
                skipTracks(skipData.getSkipCount(), skipData.getSeekOption());
            });

            addListener(SEEK_FOLDER, message -> {
                SkipData skipData = message.getData(SkipData.class);
                seekFolder(skipData.getSeekOption(), skipData.getSkipCount());
            });

            addListener(GOTO, message -> {
                Integer seconds = message.getData(Integer.class);
                if (seconds == null) {
                    return;
                }

                gotoSecond(seconds);
            });

            addListener(MUTE, message -> mute());
            addListener(UNMUTE, message -> unMute());

            addListener(SET_VOLUME, message -> {
                float volume = message.getData(Float.class);
                setVolume(volume);
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
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
    public int getTracksCount() {
        return listTracks.size();
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
    public synchronized void skipTracks(int skipCount, SeekOption option) {
        int newIndex;
        if (option == NEXT) {
            newIndex = playerStatusData.getCurrentTrackIndex() + skipCount;
            if (newIndex >= listTracks.size()) {
                newIndex = 0;
            }
        } else {
            newIndex = playerStatusData.getCurrentTrackIndex() - skipCount;
            if (newIndex < 0) {
                newIndex = listTracks.size() - 1;
            }
        }

        play(newIndex);
    }

    @Override
    public List<File> getTrackFiles() {
        return listTracks.stream()
                .map(Track::getDataSource)
                .collect(Collectors.toCollection(CollectionUtil::newList));
    }

    @Override
    public List<Track> getTracks() {
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

        final List<Artist> listArtists = CollectionUtil.newList(setArtists);
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

        final List<Album> listAlbums = CollectionUtil.newList(setAlbums);
        listAlbums.sort(Comparator.comparing(Album::getName));
        return listAlbums;
    }

    @Override
    public void addResponseListener(PlayerResponseListener responseListener) {
        addListener(PLAYER_RESPONSE, message ->
                responseListener.onPlayerResponse(
                        message.getData(PlayerInfo.class)));
    }

    @Override
    public void removeAllListeners() {
        PLAYER_BUS.unsubscribeAll();
    }

    @Override
    public void removeAllResponseListeners() {
        try {
            PLAYER_BUS.unsubscribeAll(PLAYER_RESPONSE.name());
        } catch (BusException ignored) {}
    }

    @Override
    public AtomicReference<Track> getCurrentTrack() {
        return currentTrack;
    }

    @Override
    public File getCurrentTrackFolder() {
        Track current = currentTrack.get();
        return current != null && current.getDataSource() != null
                ? current.getDataSource().getParentFile() : null;
    }

    @Override
    public int getCurrentFolderNumber() {
        File currentTrackFolder = getCurrentTrackFolder();
        if (currentTrackFolder == null) {
            return -1;
        }

        return listFolders.indexOf(currentTrackFolder) + 1;
    }

    @Override
    public int getNextFolderNumber() {
        if (listFolders.isEmpty()) {
            return -1;
        }

        int currentFolderNumber = getCurrentFolderNumber();
        return currentFolderNumber == listFolders.size() ? 1 : currentFolderNumber + 1;
    }

    @Override
    public int getPreviousFolderNumber() {
        if (listFolders.isEmpty()) {
            return -1;
        }

        int currentFolderNumber = getCurrentFolderNumber();
        return currentFolderNumber == 1 ? listFolders.size() : currentFolderNumber - 1;
    }

    @Override
    public File getFolder(int number) {
        if (number > listFolders.size() || number < 1) {
            return null;
        }

        return listFolders.get(number - 1);
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
    public boolean hasSounds() {
        return !listTracks.isEmpty();
    }

    @Override
    public boolean isValidRootFolder() {
        return rootFolder != null && rootFolder.exists();
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
    public synchronized void seekFolder(SeekOption option) {
        seekFolder(option, 1);
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
            mprisUtil.setPlaybackStatus(PlaybackStatus.Playing);
        }
    }

    @Override
    public void play(int index) {
        if (index < 0 || index >= getTracksCount()) {
            return;
        }

        muPlayerUtil.playTrackFromIndex(index);
        muPlayerUtil.sendPlayerInfoEvent();
    }

    @Override
    public synchronized void playNext() {
        int nextIndex = AudioFileUtil.getIndexFromOption(NEXT, playerStatusData, getTracksCount());
        play(nextIndex);
    }

    @Override
    public synchronized void playPrevious() {
        int prevIndex = AudioFileUtil.getIndexFromOption(PREV, playerStatusData, getTracksCount());
        play(prevIndex);
    }

    @Override
    public void playFolder(String path) {
        final Predicate<Track> filter = FilterUtil.getPlayFolderFilter(path);
        final TrackIndexed trackIndexed = muPlayerUtil.getTrackIndexedFromCondition(filter);

        if (trackIndexed != null) {
            play(trackIndexed.getIndex());
        }
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

    // Reproduce archivo de audio en la lista
    // (is alive)
    // TODO REVISAR
    @Override
    public synchronized void play(File trackFile) {
        Predicate<Track> filter = FilterUtil.getTrackFilterByPath(trackFile.getPath());
        TrackIndexed trackIndexed = muPlayerUtil.getTrackIndexedFromCondition(filter);

        if (trackIndexed == null) {
            Track newTrack = muPlayerUtil.getTrackFactory().loadTrack(trackFile);
            listTracks.add(newTrack);
            if (!CollectionUtil.existsFolder(listFolders, trackFile.getParent())) {
                listFolders.add(trackFile.getParentFile());
            }

            trackIndexed = new TrackIndexed(newTrack, getTracksCount());
        }

        play(trackIndexed.getIndex());
    }

    @Override
    public synchronized void play(String trackName) {
        Predicate<Track> filter = FilterUtil.getTrackFilterByName(trackName);
        TrackIndexed trackIndexed = muPlayerUtil.getTrackIndexedFromCondition(filter);

        if (trackIndexed != null) {
            play(trackIndexed.getIndex());
        }
    }

    @Override
    public synchronized void pause() {
        if (currentTrack.get() != null) {
            currentTrack.get().pause();
            mprisUtil.setPlaybackStatus(PlaybackStatus.Paused);
        }

    }

    @Override
    public synchronized void resumeTrack() {
        if (currentTrack.get() != null) {
            currentTrack.get().resumeTrack();
            mprisUtil.setPlaybackStatus(PlaybackStatus.Playing);
        }
    }

    @Override
    public synchronized void stopTrack() {
        if (currentTrack.get() != null) {
            currentTrack.get().stopTrack();
            mprisUtil.setPlaybackStatus(PlaybackStatus.Stopped);
        }
    }

    @Override
    public void reload() {
        if (rootFolder == null) {
            return;
        }

        playerStatusData.setCurrentTrackIndex(-1);
        playerStatusData.setMute(false);
        playerStatusData.setOn(true);
        playerStatusData.setVolume(DEFAULT_VOLUME);

        loadTracks(rootFolder);
        playNext();
    }

    // SeekedState o monitorear mejor el Playing?
    @Override
    public synchronized void seek(double seconds) {
        Track current = currentTrack.get();
        if (current == null) {
            return;
        }

        try {
            current.seek(seconds);
            mprisUtil.sendSeekedSignal(current.getProgress());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void gotoSecond(double second) {
        Track current = currentTrack.get();
        if (current == null) {
            return;
        }

        current.gotoSecond(second);
        mprisUtil.sendSeekedSignal(current.getProgress());
    }

    @Override
    public synchronized float getVolume() {
        return currentTrack.get() == null
                ? playerStatusData.getVolume() : currentTrack.get().getVolume();
    }

    // 0-100
    @Override
    public synchronized void setVolume(float volume) {
        float normalizedVolume = NumberUtil.normalizePercentValue(volume);
        playerStatusData.setVolume(normalizedVolume);
        Track current = currentTrack.get();
        if (current == null) {
            return;
        }

        current.setVolume(normalizedVolume);
        mprisUtil.sendPropertiesChangedEvent("Volume", normalizedVolume);
    }

    @Override
    public synchronized void mute() {
        playerStatusData.setMute(true);
        Track current = currentTrack.get();
        if (current == null) {
            return;
        }

        current.mute();
        mprisUtil.sendPropertiesChangedEvent("Volume", 0f);
    }

    @Override
    public synchronized void unMute() {
        if (playerStatusData.isVolumeZero()) {
            playerStatusData.setVolume(100);
        } else {
            playerStatusData.setMute(false);
        }

        Track current = currentTrack.get();
        if (current == null) {
            return;
        }

        current.unMute();
        mprisUtil.sendPropertiesChangedEvent("Volume", current.getVolume());
    }

    @Override
    public double getProgress() {
        return currentTrack.get() == null ? 0 : currentTrack.get().getProgress();
    }

    @Override
    public long getDuration() {
        Track current = currentTrack.get();
        if (current == null) {
            return 0;
        }

        return current.getDuration();
    }

    @Override
    public long getTotalDuration() {
        return listTracks.parallelStream()
                .map(Track::getDuration)
                .reduce(Long::sum).orElse(0L);
    }

    @Override
    public synchronized void shutdown() {
        playerStatusData.setOn(false);
        PLAYER_BUS.unsubscribeAll();
        if (mprisUtil.isMprisActive()) {
            mpris.shutdown();
        }

        this.interrupt();
        interruptor.switchOn();
    }

    @Override
    public void sendEvent(Message message) {
        try {
            if (message == null) {
                return;
            }

            PLAYER_BUS.publish(message);
        } catch (BusException ignored) {}
    }

    @Override
    public void addListener(PlayerEventTopics topic, MessageListener listener) {
        try {
            PLAYER_BUS.subscribe(topic.name(), listener);
        } catch (BusException ignored) {}
    }

    @Override
    public float getSystemVolume() {
        return audioSystemManager.getConsoleMasterVolume();
    }

    @Override
    public void setSystemVolume(float volume) {
        audioSystemManager.setConsoleMasterVolume((int) volume);
    }

    @Override
    public void run() {
        CACHE.set(CacheVar.PLAYER, this);
        if (mpris != null) {
            mpris.start();
        }

        loadTracks(rootFolder);
        playNext();

        // TODO: aplicar logica de reinicios y apagado de mpris
        playerStatusData.setOn(true);
        while (playerStatusData.isOn() && !isInterrupted()) {
            interruptor.checkSignal();
        }

        muPlayerUtil.killCurrentTrackIfActive();
        muPlayerUtil.clearLists();
    }

}
