package cl.estencia.labs.muplayer.util;

import cl.estencia.labs.muplayer.audio.track.Track;

import java.io.File;
import java.io.FileFilter;
import java.util.function.Predicate;

public class FilterUtil {
    private final AudioFormatSupport audioFormatSupport;

    public FilterUtil() {
        audioFormatSupport = new AudioFormatSupport();
    }

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

    public Predicate<Track> getPlayByPathFilter(String trackPath) {
        return track -> {
            File dataSource = track.getDataSource();
            return dataSource != null && dataSource.getPath().equals(trackPath);
        };
    }

    public Predicate<Track> getPlayByNameFilter(String trackName) {
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

    public FileFilter getAudioFileFilter() {
        return pathname -> getBaseFilter().accept(pathname) && audioFormatSupport.isSupportedFile(pathname);
    }

}
