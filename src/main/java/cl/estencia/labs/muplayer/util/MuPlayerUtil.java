package cl.estencia.labs.muplayer.util;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.player.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.track.StandardTrackFactory;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.TrackFactory;
import cl.estencia.labs.muplayer.exception.FormatNotSupportedException;
import cl.estencia.labs.muplayer.listener.PlayerEventType;
import cl.estencia.labs.muplayer.listener.PlayerListener;
import cl.estencia.labs.muplayer.listener.event.PlayerEvent;
import cl.estencia.labs.muplayer.listener.notifier.PlayerEventNotifier;
import cl.estencia.labs.muplayer.model.AudioFileExtension;
import cl.estencia.labs.muplayer.model.SeekOption;
import cl.estencia.labs.muplayer.model.TrackIndexed;
import lombok.extern.java.Log;

import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static cl.estencia.labs.muplayer.listener.PlayerEventType.CHANGED_CURRENT_TRACK;
import static cl.estencia.labs.muplayer.model.SeekOption.NEXT;
import static cl.estencia.labs.muplayer.thread.ThreadUtil.generateTrackThreadName;

@Log
public class MuPlayerUtil {
    private final Player player;
    private final AtomicReference<Track> current;
    private final List<Track> listTracks;
    private final List<File> listFolders;
    private final PlayerStatusData playerStatusData;
    private final PlayerEventNotifier playerEventNotifier;

    private final TrackFactory trackFactory;
    private final FilterUtil filterUtil;

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

    public MuPlayerUtil(Player player, PlayerStatusData playerStatusData, PlayerEventNotifier playerEventNotifier) {
        this.player = player;
        this.current = player.getCurrentTrack();
        this.listTracks = player.getTracks();
        this.listFolders = player.getListFolders();
        this.playerStatusData = playerStatusData;
        this.playerEventNotifier = playerEventNotifier;
        this.trackFactory = new StandardTrackFactory();
        this.filterUtil = new FilterUtil();

        configurePlayer();
    }

    private void configurePlayer() {
        this.playerEventNotifier.addInternalListener(createDefaultListener());
        this.playerEventNotifier.start();
    }

    private boolean existsNewIndex() {
        return playerStatusData.getNewTrackIndex() != NULL_INDEX_VALUE;
    }

    private void deleteNewIndex() {
        playerStatusData.setNewTrackIndex(NULL_INDEX_VALUE);
    }

    private int getIndexToPlay(SeekOption seekOption) {
        return existsNewIndex()
                ? playerStatusData.getNewTrackIndex()
                : getIndexFromOption(seekOption);
    }

    private void moveNewIndexCursorIfNotExists(SeekOption seekOption) {
        if (!existsNewIndex()) {
            final int index = getIndexToPlay(seekOption);
            playerStatusData.setNewTrackIndex(index);
        }
    }

    public Track loadTrackFromFile(File audioFile) {
        try {
            Track track = trackFactory.getTrack(audioFile, player);
            if (track != null) {
                loadTrackEvents(track);
            }
            return track;
        } catch (FormatNotSupportedException e) {
            log.severe("Error on load track ("
                    + e.getClass().getSimpleName()
                    + "): " + e.getMessage());
            return null;
        }
    }

    private void recreateCurrentTrackIfExists() {
        if (current.get() != null) {
            Track recreatedTrack = loadTrackFromFile(current.get().getDataSource());
            int currentTrackIndex = playerStatusData.getCurrentTrackIndex();

            listTracks.set(currentTrackIndex, recreatedTrack);
        }
    }

    private PlayerListener createDefaultListener() {
        return new PlayerListener() {
            @Override
            public void onPreStart(PlayerEvent event) {
                log.info("Pre start");
            }

            @Override
            public void onStarted(PlayerEvent event) {
                log.info("Player started!!");
            }

            @Override
            public void onUpdateTrackList(PlayerEvent event) {
                log.info("Updated track list!!");
            }

            @Override
            public void onCurrentTrackChange(PlayerEvent event) {
                Track current = event.player().getCurrentTrack().get();
                if (current != null) {
                    log.info("New current: " + current.getTitle());
                } else {
                    log.info("New current is null");
                }
            }

            @Override
            public void onShutdown(PlayerEvent event) {
                event.listFolders().clear();
                event.listTracks().clear();

                event.player().removeAllListeners();
            }
        };
    }

    private void loadTrackEvents(Track track) {
        track.getNotifier().addInternalListener(trackEvent -> {
            switch (trackEvent.trackStateName()) {
                case FINISHED -> {
                    moveNewIndexCursorIfNotExists(NEXT);
                    playTrackByNewIndex();
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
        });
    }

    public synchronized int getSongsCount() {
        return listTracks.size();
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
            AudioFileExtension.valueOf(fileFormatName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized void playTrackByNewIndex() {
        final int index = playerStatusData.getNewTrackIndex();

        recreateCurrentTrackIfExists();
        playerStatusData.setCurrentTrackIndex(index);
        deleteNewIndex();

        current.set(listTracks.get(index));
        startTrackThread(current.get());

        playerEventNotifier.sendEvent(createPlayerEvent(CHANGED_CURRENT_TRACK));
    }

    public void waitForSongs() {
        int songsCount;
        while (playerStatusData.isOn() && (songsCount = getSongsCount()) == 0) {
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

    public void startTrackThread(Track current) {
        if (current != null) {
            current.setName(generateTrackThreadName(current.getClass(), current));
            current.setVolume(playerStatusData.getVolume());
            if (playerStatusData.isMute()) {
                current.mute();
            }
            current.start();
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
        final int tracksSize = listTracks.size();

        int newIndex;
        if (seekOption == SeekOption.NEXT) {
            newIndex = currentIndex == tracksSize - 1 ? 0 : currentIndex + 1;
        } else {
            newIndex = currentIndex == 0 ? tracksSize - 1 : currentIndex - 1;
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
