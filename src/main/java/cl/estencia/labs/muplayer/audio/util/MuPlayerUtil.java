package cl.estencia.labs.muplayer.audio.util;

import cl.estencia.labs.ebot.bus.MessageBus;
import cl.estencia.labs.muplayer.audio.model.TrackIndexed;
import cl.estencia.labs.muplayer.audio.model.TrackStatusData;
import cl.estencia.labs.muplayer.audio.player.MusicPlayer;
import cl.estencia.labs.muplayer.audio.model.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.factory.StandardTrackFactory;
import cl.estencia.labs.muplayer.audio.track.factory.TrackFactory;
import cl.estencia.labs.muplayer.core.bus.message.Events;
import cl.estencia.labs.muplayer.core.bus.util.MessageBusUtil;
import cl.estencia.labs.muplayer.audio.common.enums.SeekOption;
import cl.estencia.labs.muplayer.core.exception.AudioFileInvalidException;
import cl.estencia.labs.muplayer.core.exception.FormatNotSupportedException;
import cl.estencia.labs.muplayer.core.util.FilterUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static cl.estencia.labs.muplayer.audio.util.AudioFileUtil.isSupportedAudioFile;
import static cl.estencia.labs.muplayer.core.thread.ThreadUtil.generateTrackThreadName;

@Slf4j
public class MuPlayerUtil {
    private final MusicPlayer player;
    private final List<Track> listTracks;
    private final List<File> listFolders;
    private final PlayerStatusData playerStatusData;
    private final TrackFactory trackFactory;
    private final MessageBus messageBus;

    public MuPlayerUtil(MusicPlayer player, PlayerStatusData playerStatusData) {
        this.player = player;
        this.listTracks = player.getTracks();
        this.listFolders = player.getListFolders();
        this.playerStatusData = playerStatusData;
        this.trackFactory = TrackFactory.newFactory();
        this.messageBus = MessageBusUtil.getMessageBus();
    }

    public Track loadTrackFromFile(File audioFile) {
        try {
            return trackFactory.getTrack(audioFile);
        } catch (AudioFileInvalidException | FormatNotSupportedException e) {
            log.warn(e.getMessage());
            return null;
        }
    }

    public boolean isCurrentTrackActive() {
        AtomicReference<Track> currentTrack = player.getCurrentTrack();
        return currentTrack.get() != null  && currentTrack.get().isActive();
    }

    public void killActiveTracks() {
        listTracks.parallelStream()
                .filter(Track::isActive)
                .forEach(Track::kill);
    }

    public void killCurrentTrackIfActive() {
        if (isCurrentTrackActive()) {
            player.getCurrentTrack().get().kill();
        }
    }

    public void sendTrackChangedEvent() {
        if (messageBus == null || messageBus.getState() == Thread.State.TERMINATED) {
            return;
        }

        player.sendEvent(Events.playerResponse(player.getCurrentTrack(), playerStatusData));
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

    public void reloadCurrentTrack() {
        AtomicReference<Track> currentTrack = player.getCurrentTrack();
        Track current = currentTrack.get();
        if (current == null) {
            return;
        }

        File dataSource = current.getDataSource();
        current = loadTrackFromFile(dataSource);
        if (current == null) {
            return;
        }

        listTracks.set(playerStatusData.getCurrentTrackIndex(), current);
        killCurrentTrackIfActive();
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
            reloadCurrentTrack();
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
