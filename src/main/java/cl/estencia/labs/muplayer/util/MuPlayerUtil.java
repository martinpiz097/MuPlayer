package cl.estencia.labs.muplayer.util;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.player.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.factory.StandardTrackFactory;
import cl.estencia.labs.muplayer.audio.track.factory.TrackFactory;
import cl.estencia.labs.muplayer.exception.FormatNotSupportedException;
import cl.estencia.labs.muplayer.listener.PlayerEventType;
import cl.estencia.labs.muplayer.listener.PlayerListener;
import cl.estencia.labs.muplayer.listener.TrackStateListener;
import cl.estencia.labs.muplayer.listener.event.PlayerEvent;
import cl.estencia.labs.muplayer.listener.notifier.internal.PlayerInternalEventNotifier;
import cl.estencia.labs.muplayer.listener.notifier.internal.TrackInternalEventNotifier;
import cl.estencia.labs.muplayer.listener.notifier.user.PlayerUserEventNotifier;
import cl.estencia.labs.muplayer.model.SeekOption;
import cl.estencia.labs.muplayer.model.SupportedAudioExtensions;
import cl.estencia.labs.muplayer.model.TrackIndexed;
import cl.estencia.labs.muplayer.service.LogService;
import cl.estencia.labs.muplayer.service.impl.LogServiceImpl;
import lombok.extern.java.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static cl.estencia.labs.muplayer.listener.PlayerEventType.CHANGED_CURRENT_TRACK;
import static cl.estencia.labs.muplayer.listener.PlayerEventType.UPDATED_TRACK_LIST;
import static cl.estencia.labs.muplayer.model.SeekOption.NEXT;
import static cl.estencia.labs.muplayer.thread.ThreadUtil.generateTrackThreadName;

@Log
public class MuPlayerUtil {
    private final Player player;
    private final List<Track> listTracks;
    private final List<File> listFolders;
    private final PlayerStatusData playerStatusData;
    private final PlayerInternalEventNotifier internalEventNotifier;

    private final TrackFactory trackFactory;
    private final FilterUtil filterUtil;

    private final List<TrackStateListener> listInternalTrackListeners;

    private final LogService logService;

    private static final byte NULL_INDEX_VALUE = Byte.MIN_VALUE;

    public static final Comparator<Track> TRACKS_SORT_COMPARATOR = (o1, o2) -> {
        if (o1 == null || o2 == null) {
            return 0;
        }
        final File dataSource1 = o1.getDataSource();
        final File dataSource2 = o2.getDataSource();
        return dataSource1.getPath().compareTo(dataSource2.getPath());
    };

    public static final Comparator<File> FOLDERS_COMPARATOR = Comparator.comparing(File::getPath);

    public MuPlayerUtil(Player player, PlayerStatusData playerStatusData,
                        PlayerInternalEventNotifier internalEventNotifier,
                        List<TrackStateListener> listInternalTrackListeners) {
        this.player = player;
        this.listTracks = player.getTracks();
        this.listFolders = player.getListFolders();
        this.playerStatusData = playerStatusData;
        this.internalEventNotifier = internalEventNotifier;
        this.trackFactory = new StandardTrackFactory();
        this.filterUtil = new FilterUtil();
        this.listInternalTrackListeners = listInternalTrackListeners;

        this.logService = new LogServiceImpl();
    }

    private boolean existsNewIndex(PlayerStatusData playerStatusData) {
        return playerStatusData.getNewTrackIndex() != NULL_INDEX_VALUE;
    }

    private int getIndexToPlay(SeekOption seekOption, PlayerStatusData playerStatusData) {
        return existsNewIndex(playerStatusData)
                ? playerStatusData.getNewTrackIndex()
                : getIndexFromOption(seekOption);
    }

    private void moveNewIndexCursorIfNotExists(SeekOption seekOption, PlayerStatusData playerStatusData) {
        if (!existsNewIndex(playerStatusData)) {
            final int index = getIndexToPlay(seekOption, playerStatusData);
            playerStatusData.setNewTrackIndex(index);
        }
    }

    // agregar los listeners (internals y user) del track anterior
    private void configureTrackEvents(TrackInternalEventNotifier newTrackEventNotifier) {
        if (listInternalTrackListeners.isEmpty()) {
            TrackStateListener defaultTrackEvents = createDefaultTrackEvents();
            listInternalTrackListeners.add(defaultTrackEvents);
        }

        listInternalTrackListeners.forEach(newTrackEventNotifier::addListener);
    }

    public Track loadTrackFromFile(File audioFile) {
        try {
            TrackInternalEventNotifier trackEventNotifier = new TrackInternalEventNotifier();
            Track track = trackFactory.getTrack(audioFile,
                    trackEventNotifier);

            configureTrackEvents(trackEventNotifier);
            return track;
        } catch (FormatNotSupportedException e) {
            log.severe("Error on load track ("
                    + e.getClass().getSimpleName()
                    + "): " + e.getMessage());
            return null;
        }
    }

    public void transferUserListeners(Track oldTrack, Track newTrack) {
        if (oldTrack == null || newTrack == null) {
            return;
        }

        oldTrack.getAllListeners().forEach(newTrack::addListener);
    }

    private void recreateCurrentTrackIfExists(AtomicReference<Track> currentTrack,
                                              List<Track> listTracks,
                                              PlayerStatusData playerStatusData) {
        if (currentTrack.get() != null) {
            Track recreatedTrack = loadTrackFromFile(currentTrack.get().getDataSource());
            int currentTrackIndex = playerStatusData.getCurrentTrackIndex();

            listTracks.set(currentTrackIndex, recreatedTrack);
        }
    }

    private void loadTracks(File folderToLoad) {
//        Files.find()
        try (Stream<Path> folderPaths = Files.walk(
                Path.of(folderToLoad.toURI())).parallel()) {
            if (player.hasSounds()) {
                listTracks.clear();
                listFolders.clear();
            }

            folderPaths
                    .filter(this::hasAudioFormatExtension)
                    .map(path -> loadTrackFromFile(path.toFile()))
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
        loadTracks(player.getRootFolder());
        internalEventNotifier.sendEvent(createPlayerEvent(UPDATED_TRACK_LIST));
    }

    private void checkRootFolder(Player player) throws FileNotFoundException {
        File rootFolder = player.getRootFolder();
        if (rootFolder != null && rootFolder.exists()
                && filterUtil.getDirectoriesFilter().accept(rootFolder)) {
            setupTracksList();
        } else if (rootFolder == null) {
            logService.warningLog("To set music folder run this: smf ${music-folder-path}\n");
        } else {
            throw new FileNotFoundException(rootFolder.getPath());
        }
    }

    public PlayerListener createDefaultListener() {
        return new PlayerListener() {
            @Override
            public void onPreStart(PlayerEvent event) throws FileNotFoundException {
                log.info("Pre start");
            }

            @Override
            public void onStarted(PlayerEvent event) throws FileNotFoundException {
                log.info("Start");

                Player eventPlayer = event.player();
                PlayerStatusData statusData = eventPlayer.getPlayerStatusData();

                checkRootFolder(eventPlayer);
                statusData.setOn(true);
                waitForSongs(eventPlayer);

                eventPlayer.play(0);
            }

            @Override
            public void onUpdateTrackList(PlayerEvent event) {
                log.info("Updated track list!!");
            }

            @Override
            public void onCurrentTrackChange(PlayerEvent event) {
                Player eventPlayer = event.player();
                PlayerStatusData statusData = eventPlayer.getPlayerStatusData();
                AtomicReference<Track> oldTrackRef = eventPlayer.getCurrentTrack();

                moveNewIndexCursorIfNotExists(NEXT, statusData);
                recreateCurrentTrackIfExists(oldTrackRef,
                        event.listTracks(),
                        eventPlayer.getPlayerStatusData());

                final int index = statusData.getNewTrackIndex();
                statusData.setCurrentTrackIndex(index);
                playerStatusData.setNewTrackIndex(NULL_INDEX_VALUE);

                Track oldTrack = oldTrackRef.get();
                Track newTrack = event.listTracks().get(index);
                transferUserListeners(oldTrack, newTrack);

                oldTrackRef.set(newTrack);
                startTrackThread(oldTrackRef.get());

                if (newTrack != null) {
                    log.info("New current track: " + newTrack.getTitle());
                } else {
                    log.info("New current track is null");
                }

            }

            @Override
            public void onShutdown(PlayerEvent event) {
                event.listFolders().clear();
                event.listTracks().clear();

                event.player().removeAllPlayerListeners();
            }
        };
    }

    // idealmente que aca vayan operaciones especificas del player, las de track deberian gestionarse
    // en los estados
    private TrackStateListener createDefaultTrackEvents() {
        return trackEvent -> {
            switch (trackEvent.trackStateName()) {
                case FINISHED -> {
                    internalEventNotifier.sendEvent(createPlayerEvent(CHANGED_CURRENT_TRACK));
                }
                case PAUSED -> {
                }
                case PLAYING -> {
                }
                case REVERBERATED -> {

                }
                case STARTED -> {
                }
                case STOPPED -> {
                }
                case UNKNOWN -> {
                }
            }
        };
    }

    public boolean hasAudioFormatExtension(Path audioFilePath) {
        return audioFilePath != null && hasAudioFormatExtension(audioFilePath.toFile());
    }

    public boolean hasAudioFormatExtension(File audioFile) {
        if (audioFile == null || !audioFile.exists() || audioFile.isDirectory()) {
            return false;
        }

        String fileFormatName = FileUtil.getFileFormatName(audioFile);

        try {
            SupportedAudioExtensions.valueOf(fileFormatName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void waitForSongs(Player player) {
        PlayerStatusData statusData = player.getPlayerStatusData();

        int songsCount;
        while (statusData.isOn() && (songsCount = player.getSongsCount()) == 0) {
            try {
                log.info("WaitForSongs::Songs count: " + songsCount);
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public int getFolderIndex(Track current) {
        if (current != null) {
            final File dataSource = current.getDataSource();
            final File currentParent = dataSource.getParentFile();
            return listFolders.indexOf(currentParent);
        } else {
            return -1;
        }
    }

    public void startTrackThread(Track currentTrack) {
        if (currentTrack != null) {
            currentTrack.setName(generateTrackThreadName(currentTrack.getClass(), currentTrack));
            currentTrack.setVolume(playerStatusData.getVolume());
            if (playerStatusData.isMute()) {
                currentTrack.mute();
            }
            currentTrack.start();
        }
    }

    // si incluyo paralelismo en este metodo, debo crear otro o gestionar con parametro boolean,
    // ya que hay algunos casos en los que si necesito secuencialidad
    public TrackIndexed getTrackIndexedFromCondition(Predicate<Track> filter) {
        int index = 0;

        for (Track track : listTracks) {
            if (filter.test(track)) {
                return new TrackIndexed(track, index);
            }
            index++;
        }
        return null;
    }

    public TrackIndexed findFirstIn(String folderPath) {
        final File parentFile = new File(folderPath);
        final Predicate<Track> filter = filterUtil.getFindFirstInFilter(parentFile);

        return getTrackIndexedFromCondition(filter);
    }

    public int seekToFolder(String folderPath) {
        final File parentFile = new File(folderPath);

        // idea para electrolist -> Indexof con predicate
        Predicate<Track> filter = filterUtil.newSeekToFolderFilter(parentFile);
        TrackIndexed trackIndexed = getTrackIndexedFromCondition(filter);

        return trackIndexed != null ? trackIndexed.getIndex() : -1;
    }

    public int getIndexFromOption(SeekOption seekOption) {
        final int currentIndex = playerStatusData.getCurrentTrackIndex();
        final int tracksCount = listTracks.size();

        int newIndex;
        if (seekOption == SeekOption.NEXT) {
            newIndex = currentIndex == tracksCount - 1 ? 0 : currentIndex + 1;
        } else {
            newIndex = currentIndex == 0 ? tracksCount - 1 : currentIndex - 1;
        }

        return newIndex;
    }

    public Track getTrackBySeekOption(SeekOption seekOption) {
        return listTracks.get(getIndexFromOption(seekOption));
    }

    public PlayerEvent createPlayerEvent(PlayerEventType type) {
        return new PlayerEvent(type, player, listTracks, listFolders);
    }

}
