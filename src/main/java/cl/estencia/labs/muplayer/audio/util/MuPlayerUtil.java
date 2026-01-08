package cl.estencia.labs.muplayer.audio.util;

import cl.estencia.labs.ebot.bus.MessageBus;
import cl.estencia.labs.muplayer.audio.model.TrackIndexed;
import cl.estencia.labs.muplayer.audio.model.TrackStatusData;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.model.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.factory.StandardTrackFactory;
import cl.estencia.labs.muplayer.audio.track.factory.TrackFactory;
import cl.estencia.labs.muplayer.core.bus.util.MessageBusUtil;
import cl.estencia.labs.muplayer.audio.common.enums.SeekOption;
import cl.estencia.labs.muplayer.core.service.LogService;
import cl.estencia.labs.muplayer.core.service.impl.LogServiceImpl;
import cl.estencia.labs.muplayer.core.util.FilterUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
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

import static cl.estencia.labs.muplayer.audio.util.AudioFileUtil.isSupportedAudioFile;
import static cl.estencia.labs.muplayer.core.thread.ThreadUtil.generateTrackThreadName;

@Slf4j
public class MuPlayerUtil {
    private final Player player;
    private final List<Track> listTracks;
    private final List<File> listFolders;
    private final PlayerStatusData playerStatusData;

    private final TrackFactory trackFactory;

    private final MessageBus messageBus;

    private static final byte NULL_INDEX_VALUE = Byte.MIN_VALUE;

    public MuPlayerUtil(Player player, PlayerStatusData playerStatusData) {
        this.player = player;
        this.listTracks = player.getTracks();
        this.listFolders = player.getListFolders();
        this.playerStatusData = playerStatusData;
        this.trackFactory = new StandardTrackFactory();

        this.messageBus = MessageBusUtil.getMessageBus();
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

    public boolean isCurrentTrackActive() {
        AtomicReference<Track> currentTrack = player.getCurrentTrack();
        return currentTrack.get() != null  && currentTrack.get().isActive();
    }

    public void killCurrentTrackIfActive() {
        if (isCurrentTrackActive()) {
            player.getCurrentTrack().get().kill();
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
            TrackStatusData trackStatusData = currentTrack.getTrackStatusData();

            currentTrack.setName(generateTrackThreadName(currentTrack.getClass(), currentTrack));
            trackStatusData.setVolume(playerStatusData.getVolume());
            trackStatusData.setMute(playerStatusData.isMute());

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

    public Comparator<File> createFoldersComparator() {
        return Comparator.comparing(File::getPath);
    }

    public Comparator<File> createTrackFilesSortComparator() {
        return (o1, o2) -> {
            if (o1 == null || o2 == null) {
                return 0;
            }

            return o1.getPath().compareTo(o2.getPath());
        };
    }

    public Comparator<Path> createTrackPathsSortComparator() {
        return (o1, o2) -> {
            if (o1 == null || o2 == null) {
                return 0;
            }

            return createTrackFilesSortComparator().compare(o1.toFile(), o2.toFile());
        };
    }

    public Comparator<Track> createTracksSortComparator() {
        return (o1, o2) -> {
            if (o1 == null || o2 == null) {
                return 0;
            }

            final File dataSource1 = o1.getDataSource();
            final File dataSource2 = o2.getDataSource();
            return createTrackFilesSortComparator().compare(dataSource1, dataSource2);
        };
    }

    public FileFilter createTrackFileFilter() {
        return pathname -> !pathname.isDirectory() && isSupportedAudioFile(pathname);
    }

    public FileFilter createFolderFilter() {
        return pathname -> {
            if (!pathname.isDirectory()) {
                return false;
            }

            String[] list = pathname.list();
            return list != null && list.length > 0;
        };
    }

}
