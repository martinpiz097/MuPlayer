package cl.estencia.labs.muplayer.audio.track;

import cl.estencia.labs.muplayer.audio.track.factory.TrackFactory;
import cl.estencia.labs.muplayer.audio.util.AudioFileUtil;
import cl.estencia.labs.muplayer.core.exception.AudioFileInvalidException;
import cl.estencia.labs.muplayer.core.exception.FormatNotSupportedException;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import lombok.Getter;

import java.io.File;
import java.util.Comparator;
import java.util.List;

@Getter
public class TracksDirectory {
    private final File folder;
    private final List<Track> tracks;
    private final TrackFactory trackFactory;
    private final Comparator<Track> tracksComparator;

    public TracksDirectory(File folder, Comparator<Track> tracksComparator) {
        this.folder = folder;
        this.trackFactory = TrackFactory.newFactory();
        this.tracksComparator = tracksComparator;
        this.tracks = CollectionUtil.newFastArrayList();
    }

    // tendre una doble validacion porque esto lo revisa el trackFactory
    // pero se hace para entregar una lista mas limpia
    public void scanDirectory() {
        if (!hasTracks()) {
            tracks.clear();
        }

        final File[] files = folder.listFiles(pathname ->
                pathname.isFile() && AudioFileUtil.isSupportedAudioFile(pathname));
        if (files == null || files.length == 0) {
            return;
        }

        final int filesCount = files.length;
        Track track;
        for (int i = 0; i < filesCount; i++) {
            track = trackFactory.loadTrack(files[i]);
            if (track == null) {
                return;
            }

            tracks.add(track);
        }

        if (tracks.isEmpty()) {
            return;
        }

        tracks.sort(tracksComparator);
    }

    public boolean hasTracks() {
        return !tracks.isEmpty();
    }

    public String getName() {
        return folder.getName();
    }

    public String getPath() {
        return folder.getPath();
    }

    public int getTracksCount() {
        return tracks.size();
    }

    @Override
    public String toString() {
        return "TracksDirectory{" +
                "folder=" + folder.getPath() +
                '}';
    }
}
