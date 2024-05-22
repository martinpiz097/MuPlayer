package org.muplayer.util;

import org.muplayer.audio.track.Track;

import java.io.File;
import java.io.FileFilter;
import java.util.function.Predicate;

public class FilterUtil {
    public static Predicate<Track> newSeekToFolderFilter(File parentFile) {
        return track -> {
            File dataSource = track.getDataSource();
            return dataSource != null && dataSource.getParentFile().equals(parentFile);
        };
    }

    public static Predicate<Track> getPlayFolderFilter(String fldPath) {
        return track -> {
            File dataSource = track.getDataSource();
            return dataSource != null && dataSource.getParent().equals(fldPath);
        };
    }

    public static Predicate<Track> getPlayByPathFilter(String trackPath) {
        return track -> {
            File dataSource = track.getDataSource();
            return dataSource != null && dataSource.getPath().equals(trackPath);
        };
    }

    public static Predicate<Track> getPlayByNameFilter(String trackName) {
        return track -> {
            File dataSource = track.getDataSource();
            return dataSource != null && dataSource.getName().equals(trackName);
        };
    }

    public static Predicate<Track> getFindFirstInFilter(File parentFile) {
        return track -> {
            File dataSource = track.getDataSource();
            return dataSource != null && dataSource.getParentFile().equals(parentFile);
        };
    }

    public static FileFilter getBaseFilter() {
        return pathname -> pathname.canRead() && !pathname.isHidden();
    }

    public static FileFilter getDirectoriesFilter() {
        return pathname -> getBaseFilter().accept(pathname) && pathname.isDirectory();
    }

    public static FileFilter getAudioFileFilter() {
        return pathname -> getBaseFilter().accept(pathname) && AudioUtil.isSupportedFile(pathname);
    }

}
