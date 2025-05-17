package cl.estencia.labs.muplayer.event.listener.impl;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.player.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.factory.TrackFactory;
import cl.estencia.labs.muplayer.core.exception.FormatNotSupportedException;
import cl.estencia.labs.muplayer.event.listener.PlayerListener;
import cl.estencia.labs.muplayer.event.listener.TrackStateListener;
import cl.estencia.labs.muplayer.event.model.PlayerEvent;
import cl.estencia.labs.muplayer.event.notifier.internal.PlayerInternalEventNotifier;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;
import cl.estencia.labs.muplayer.model.SeekOption;
import cl.estencia.labs.muplayer.service.LogService;
import cl.estencia.labs.muplayer.service.impl.LogServiceImpl;
import cl.estencia.labs.muplayer.util.AudioFormatUtil;
import cl.estencia.labs.muplayer.util.FilterUtil;
import cl.estencia.labs.muplayer.util.MuPlayerUtil;
import lombok.extern.java.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static cl.estencia.labs.muplayer.event.listener.PlayerEventType.UPDATED_TRACK_LIST;
import static cl.estencia.labs.muplayer.model.SeekOption.NEXT;
import static cl.estencia.labs.muplayer.thread.ThreadUtil.generateTrackThreadName;

@Log
public class DefaultPlayerListener implements PlayerListener {

    private final List<TrackStateListener> listInternalTrackListeners;

    private final PlayerInternalEventNotifier playerInternalEventNotifier;
    private final TrackStateListener defaultTrackStateListener;
    private final TrackFactory trackFactory;


    private final AudioFormatUtil audioFormatUtil;
    private final FilterUtil filterUtil;
    private final LogService logService;

    public DefaultPlayerListener(List<TrackStateListener> listInternalTrackListeners,
                                 PlayerInternalEventNotifier playerInternalEventNotifier, TrackStateListener defaultTrackStateListener,
                                 TrackFactory trackFactory) {
        this.listInternalTrackListeners = listInternalTrackListeners;
        this.playerInternalEventNotifier = playerInternalEventNotifier;
        this.defaultTrackStateListener = defaultTrackStateListener;
        this.trackFactory = trackFactory;
        this.audioFormatUtil = new AudioFormatUtil();
        this.filterUtil = new FilterUtil();
        this.logService = new LogServiceImpl();
    }

    private static final byte NULL_INDEX_VALUE = Byte.MIN_VALUE;

    private boolean existsNewIndex(PlayerStatusData playerStatusData) {
        return playerStatusData.getNewTrackIndex() != NULL_INDEX_VALUE;
    }

    private int getIndexToPlay(SeekOption seekOption, PlayerStatusData playerStatusData,
                               List<Track> listTracks) {
        return existsNewIndex(playerStatusData)
                ? playerStatusData.getNewTrackIndex()
                : audioFormatUtil.getIndexFromOption(seekOption, playerStatusData,
                listTracks.size());
    }

    private void moveNewIndexCursorIfNotExists(SeekOption seekOption,
                                               PlayerStatusData playerStatusData,
                                               List<Track> listTracks) {
        if (!existsNewIndex(playerStatusData)) {
            final int index = getIndexToPlay(seekOption, playerStatusData, listTracks);
            playerStatusData.setNewTrackIndex(index);
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

    public void startTrackThread(Track currentTrack, PlayerStatusData playerStatusData) {
        if (currentTrack != null) {
            currentTrack.setName(generateTrackThreadName(currentTrack.getClass(), currentTrack));
            currentTrack.setVolume(playerStatusData.getVolume());
            if (playerStatusData.isMute()) {
                currentTrack.mute();
            }
            currentTrack.start();
        }
    }

    private void checkRootFolder(PlayerEvent playerEvent) throws FileNotFoundException {
        File rootFolder = playerEvent.player().getRootFolder();
        if (rootFolder != null && rootFolder.exists()
                && filterUtil.getDirectoriesFilter().accept(rootFolder)) {
            setupTracksList(playerEvent);
        } else if (rootFolder == null) {
            logService.warningLog("To set music folder run this: smf ${music-folder-path}\n");
        } else {
            throw new FileNotFoundException(rootFolder.getPath());
        }
    }

    // agregar los listeners (internals y user) del track anterior
    private void configureTrackEvents(TrackInternalEventNotifier newTrackEventNotifier) {
        if (listInternalTrackListeners.isEmpty()) {
            listInternalTrackListeners.add(defaultTrackStateListener);
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

    private void loadTracks(Player player, List<Track> listTracks, List<File> listFolders,
                            File folderToLoad) {
//        Files.find()
        try (Stream<Path> folderPaths = Files.walk(
                Path.of(folderToLoad.toURI())).parallel()) {
            if (player.hasSounds()) {
                listTracks.clear();
                listFolders.clear();
            }

            folderPaths
                    .filter(audioFormatUtil::hasAudioFormatExtension)
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

    private void setupTracksList(PlayerEvent playerEvent) {
        Player player = playerEvent.player();
        List<Track> listTracks = playerEvent.listTracks();
        List<File> listFolders = playerEvent.listFolders();

        loadTracks(player, listTracks, listFolders, player.getRootFolder());
        playerInternalEventNotifier.sendEvent(
                new PlayerEvent(UPDATED_TRACK_LIST, player, listTracks, listFolders));
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

    public void transferUserListeners(Track oldTrack, Track newTrack) {
        if (oldTrack == null || newTrack == null) {
            return;
        }

        oldTrack.getAllListeners().forEach(newTrack::addListener);
    }

    @Override
    public void onPreStart(PlayerEvent event) throws FileNotFoundException {
        log.info("Pre start");
    }

    @Override
    public void onStarted(PlayerEvent event) throws FileNotFoundException {
        log.info("Start");

        Player eventPlayer = event.player();
        PlayerStatusData statusData = eventPlayer.getPlayerStatusData();

        checkRootFolder(event);
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

        moveNewIndexCursorIfNotExists(NEXT, statusData, event.listTracks());
        recreateCurrentTrackIfExists(oldTrackRef,
                event.listTracks(),
                eventPlayer.getPlayerStatusData());

        final int index = statusData.getNewTrackIndex();
        statusData.setCurrentTrackIndex(index);
        statusData.setNewTrackIndex(NULL_INDEX_VALUE);

        Track oldTrack = oldTrackRef.get();
        Track newTrack = event.listTracks().get(index);
        transferUserListeners(oldTrack, newTrack);

        oldTrackRef.set(newTrack);
        startTrackThread(oldTrackRef.get(), statusData);

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
}
