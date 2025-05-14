package cl.estencia.labs.muplayer.util;

import cl.estencia.labs.muplayer.audio.track.StandardTrackFactory;
import cl.estencia.labs.muplayer.model.AudioFileExtension;
import lombok.extern.java.Log;
import cl.estencia.labs.muplayer.audio.player.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.TrackFactory;
import cl.estencia.labs.muplayer.audio.player.listener.PlayerEvent;
import cl.estencia.labs.muplayer.model.SeekOption;
import cl.estencia.labs.muplayer.model.TrackIndexed;
import cl.estencia.labs.muplayer.thread.TracksLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static cl.estencia.labs.muplayer.thread.ThreadUtil.generateTrackThreadName;

@Log
public class MuPlayerUtil {

    private final List<Track> listTracks;
    private final List<File> listFolders;
    private final List<PlayerEvent> listListeners;
    private final PlayerStatusData playerStatusData;

    private final TrackFactory trackFactory;
    private final FilterUtil filterUtil;

    public static final Comparator<Track> TRACKS_SORT_COMPARATOR = (o1, o2) -> {
        if (o1 == null || o2 == null) {
            return 0;
        }
        final File dataSource1 = o1.getDataSource();
        final File dataSource2 = o2.getDataSource();
        return dataSource1.getPath().compareTo(dataSource2.getPath());
    };

    public static final Comparator<File> FOLDERS_COMPARATOR = Comparator.comparing(File::getPath);

    public MuPlayerUtil(List<Track> listTracks, List<File> listFolders,
                        List<PlayerEvent> listListeners, PlayerStatusData playerStatusData) {
        this.listTracks = listTracks;
        this.listFolders = listFolders;
        this.listListeners = listListeners;
        this.playerStatusData = playerStatusData;

        this.trackFactory = new StandardTrackFactory();
        this.filterUtil = new FilterUtil();
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

    public void waitForTracksLoading() {
//        if (TracksLoader.getInstance().hasPendingTasks()) {
//            ThreadUtil.freezeThread(this);
//        }
        while (TracksLoader.getInstance().hasPendingTasks()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        }
    }

    public void cleanUpFoldersList() {
        List<File> listFilteredFolders = listFolders.parallelStream().distinct()
                .sorted()
                .collect(Collectors.toCollection(CollectionUtil::newFastArrayList));

        listFolders.clear();
        listFolders.addAll(listFilteredFolders);
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

//    public void loadListenerMethod(ListenerMethodName methodName, Track track) {
//        if (!listListeners.isEmpty()) {
//            final String threadName = ListenerRunner.class.getSimpleName();
//            TaskRunner.execute(new ListenerRunner(listListeners, methodName, track),
//                    threadName);
//        }
//    }

    public void restartCurrent(Track current) {
        try {
            if (current != null) {
                Track cur = trackFactory.getTrack(current.getDataSource());
                current.kill();
                listTracks.set(playerStatusData.getTrackIndex(), cur);
            }
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
    }

    public int seekToFolder(String folderPath) {
        final File parentFile = new File(folderPath);

        // idea para electrolist -> Indexof con predicate
        Predicate<Track> filter = filterUtil.newSeekToFolderFilter(parentFile);
        TrackIndexed trackIndexed = getTrackIndexedFromCondition(filter);

        return trackIndexed != null ? trackIndexed.getIndex() : -1;
    }

    public synchronized Track changeTrack(SeekOption seekOption, Track current) {
        final int currentIndex = playerStatusData.getTrackIndex();
        final int newIndex;
        final int tracksSize = listTracks.size();

        if (seekOption == SeekOption.NEXT) {
            newIndex = currentIndex == tracksSize - 1 ? 0 : currentIndex + 1;
        } else {
            newIndex = currentIndex == 0 ? tracksSize - 1 : currentIndex - 1;
        }
        return changeTrack(newIndex, current);
    }

    public synchronized Track changeTrack(int newTrackIndex, Track current) {
        final Track track = listTracks.get(newTrackIndex);
        if (track != null) {
            final TrackIndexed trackIndexed = new TrackIndexed(track, newTrackIndex);
            return changeTrack(trackIndexed, current);
        }
        return null;
    }

    public synchronized Track changeTrack(TrackIndexed newTrackIndexed, Track current) {
        if (newTrackIndexed != null) {
            restartCurrent(current);
            playerStatusData.setTrackIndex(newTrackIndexed.getIndex());
            final Track newCurrent = newTrackIndexed.getTrack();
            initCurrentTrack(newCurrent);

            return newCurrent;
        }
        return current;
    }

    private void initCurrentTrack(Track current) {
        startTrackThread(current);
        //loadListenerMethod(ON_SONG_CHANGE, current);
    }

    public int getNewIndex(SeekOption seekOption) {
        final int trackIndex = playerStatusData.getTrackIndex();
        final int songsCount = getSongsCount();

        if (seekOption == SeekOption.NEXT) {
            return trackIndex == -1 ? 0 : (trackIndex == songsCount - 1 ? 0 : trackIndex + 1);
        } else {
            return trackIndex == -1 ? songsCount - 1 : (trackIndex == 0 ? songsCount - 1 : trackIndex - 1);
        }
    }

    public Track getTrackBySeekOption(SeekOption seekOption) {
        return listTracks.get(getNewIndex(seekOption));
    }

}
