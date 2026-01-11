package cl.estencia.labs.muplayer.audio.model;

import cl.estencia.labs.muplayer.audio.track.Track;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class TracksDirectory {
    private final File folder;
    private final List<Track> tracks;
    private final AtomicInteger currentIndex;

    public TracksDirectory(File folder, List<Track> tracks) {
        this.folder = folder;
        this.tracks = tracks;
        this.currentIndex = new AtomicInteger(-1);
    }

    public int getCount() {
        return tracks.size();
    }

    public Track getTrack(int index) {
        try {
            return tracks.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public Track updateTrack(int index, Track track) {
        try {
            return tracks.set(index, track);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void sortTracks(Comparator<? super Track> comparator) {
        tracks.sort(comparator);
    }

    public void killActiveTracks() {
        tracks.parallelStream()
                .filter(Track::isActive)
                .forEach(Track::kill);

    }

    public void clearDirectory() {
        killActiveTracks();
        tracks.clear();
    }

}
