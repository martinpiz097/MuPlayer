package cl.estencia.labs.muplayer.utils;

import cl.estencia.labs.muplayer.audio.track.Track;

import java.io.File;
import java.io.FileFilter;
import java.util.function.Predicate;

public class FilterUtil {

    public Predicate<Track> newSeekToFolderFilter(File parentFile) {
        return track -> {
            File dataSource = track.getDataSource();
            return dataSource != null && dataSource.getParentFile().equals(parentFile);
        };
    }

    public Predicate<Track> getPlayFolderFilter(String fldPath) {
        return track -> {
            File dataSource = track.getDataSource();
            return dataSource != null && dataSource.getParent().equals(fldPath);
        };
    }

    public Predicate<Track> getTrackFilterByPath(String trackPath) {
        return track -> {
            File dataSource = track.getDataSource();
            return dataSource != null && dataSource.getPath().equals(trackPath);
        };
    }

    public Predicate<Track> getTrackFilterByName(String trackName) {
        return track -> {
            File dataSource = track.getDataSource();
            return dataSource != null && dataSource.getName().equals(trackName);
        };
    }

    public Predicate<Track> getFindFirstInFilter(File parentFile) {
        return track -> {
            File dataSource = track.getDataSource();
            return dataSource != null && dataSource.getParentFile().equals(parentFile);
        };
    }

    public FileFilter getBaseFilter() {
        return pathname -> pathname.canRead() && !pathname.isHidden();
    }

    public FileFilter getDirectoriesFilter() {
        return pathname -> getBaseFilter().accept(pathname) && pathname.isDirectory();
    }

}
