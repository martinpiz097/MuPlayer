package cl.estencia.labs.muplayer.util;

import cl.estencia.labs.ebot.bus.MessageBus;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.player.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.factory.StandardTrackFactory;
import cl.estencia.labs.muplayer.audio.track.factory.TrackFactory;
import cl.estencia.labs.muplayer.core.common.enums.SeekOption;
import cl.estencia.labs.muplayer.audio.model.TrackIndexed;
import cl.estencia.labs.muplayer.core.service.LogService;
import cl.estencia.labs.muplayer.core.service.impl.LogServiceImpl;
import cl.estencia.labs.muplayer.bus.MuPlayerBusUtil;
import lombok.extern.slf4j.Slf4j;

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

import static cl.estencia.labs.muplayer.core.thread.ThreadUtil.generateTrackThreadName;

@Slf4j
public class MuPlayerUtil {
    private final Player player;
    private final List<Track> listTracks;
    private final List<File> listFolders;
    private final PlayerStatusData playerStatusData;

    private final TrackFactory trackFactory;

    private final LogService logService;

    private final MessageBus messageBus;

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

    public MuPlayerUtil(Player player, PlayerStatusData playerStatusData) {
        this.player = player;
        this.listTracks = player.getTracks();
        this.listFolders = player.getListFolders();
        this.playerStatusData = playerStatusData;
        this.trackFactory = new StandardTrackFactory();

        this.logService = new LogServiceImpl();
        this.messageBus = MuPlayerBusUtil.getMessageBus();
    }

    private boolean existsNewIndex(PlayerStatusData playerStatusData) {
        return playerStatusData.getNewTrackIndex() != NULL_INDEX_VALUE;
    }

    private int getIndexToPlay(SeekOption seekOption, PlayerStatusData playerStatusData) {
        return existsNewIndex(playerStatusData)
                ? playerStatusData.getNewTrackIndex()
                : AudioFileUtil.getIndexFromOption(seekOption, playerStatusData,
                player.getSongsCount());
    }

    private void moveNewIndexCursorIfNotExists(SeekOption seekOption, PlayerStatusData playerStatusData) {
        if (!existsNewIndex(playerStatusData)) {
            final int index = getIndexToPlay(seekOption, playerStatusData);
            playerStatusData.setNewTrackIndex(index);
        }
    }

    public Track loadTrackFromFile(File audioFile) {
        try {
            return trackFactory.getTrack(audioFile);
        } catch (Exception e) {
            log.error("Error on load track ("
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
                    .filter(path -> AudioFileUtil.hasAudioFormatExtension(path.toFile()))
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
    
    private void checkRootFolder(Player player) throws FileNotFoundException {
        File rootFolder = player.getRootFolder();
        if (rootFolder != null && rootFolder.exists()
                && FilterUtil.getDirectoriesFilter().accept(rootFolder)) {
            loadTracks(player.getRootFolder());
        } else if (rootFolder == null) {
            logService.warningLog("To set music folder run this: smf ${music-folder-path}\n");
        } else {
            throw new FileNotFoundException(rootFolder.getPath());
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
        final Predicate<Track> filter = FilterUtil.getFindFirstInFilter(parentFile);

        return getTrackIndexedFromCondition(filter);
    }

    public int seekToFolder(String folderPath) {
        final File parentFile = new File(folderPath);

        // idea para electrolist -> Indexof con predicate
        Predicate<Track> filter = FilterUtil.newSeekToFolderFilter(parentFile);
        TrackIndexed trackIndexed = getTrackIndexedFromCondition(filter);
        return trackIndexed != null ? trackIndexed.getIndex() : -1;
    }

    public Track getTrackBySeekOption(SeekOption seekOption) {
        int indexFromOption = AudioFileUtil.getIndexFromOption(seekOption, playerStatusData, player.getSongsCount());

        return listTracks.get(indexFromOption);
    }

    public void restartCurrentTrack() {
        AtomicReference<Track> currentTrack = player.getCurrentTrack();
        if (currentTrack.get() != null) {
            File dataSource = currentTrack.get().getDataSource();
            Track trackFromFile = loadTrackFromFile(dataSource);
            listTracks.set(playerStatusData.getCurrentTrackIndex(), trackFromFile);

            if (currentTrack.get().isActive()) {
                synchronized (currentTrack) {
                    currentTrack.get().kill();
                }
            }
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

    public void playNewTrack(int index) {
        synchronized (player.getCurrentTrack()) {
            restartCurrentTrack();
            playerStatusData.setCurrentTrackIndex(index);

            Track newTrack = listTracks.get(index);
            player.getCurrentTrack().set(newTrack);
            startTrackThread(newTrack);
        }
    }

}
