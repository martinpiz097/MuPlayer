package org.muplayer.audio.util;

import org.muplayer.audio.Player;
import org.muplayer.audio.Track;

import java.io.File;
import java.text.DecimalFormat;

public class PlayerInfo {
    private final Track current;
    private final File trackFolder;
    private final File rootFolder;
    private final float gain;

    private int songsCount;
    private int foldersCount;

    public PlayerInfo(Player player) {
        this.current = player.getCurrent();
        trackFolder = current == null ? null : current.getDataSource().getParentFile();
        rootFolder = player.getRootFolder();
        gain = player.getGain();
        songsCount = player.getSongsCount();
        foldersCount = player.getFoldersCount();
    }

    public Track getCurrentTrack() {
        return current;
    }

    public String getCurrentTrackTitle() {
        return current == null ? "Title Unknown" : current.getTitle();
    }

    public String getCurrentTrackFormat() {
        if (current == null)
            return "Track Unknown";
        else {
            String currentClass = current.getClass().getSimpleName();
            return currentClass.substring(0, currentClass.length()-5).toLowerCase();
        }
    }

    public File getTrackFolder() {
        return trackFolder;
    }

    public File getRootFolder() {
        return rootFolder;
    }

    public float getVolume() {
        return gain;
    }

    public int getSongsCount() {
        return songsCount;
    }

    public int getFoldersCount() {
        return foldersCount;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("{\n\t")
                .append("Track: ").append(getCurrentTrackTitle()).append(",\n\t")
                .append("Track Format: ").append(getCurrentTrackFormat()).append(",\n\t")
                .append("Track Folder: ").append(trackFolder==null?"Unknown":trackFolder.getPath()).append(",\n\t")
                .append("Root Folder: ").append(rootFolder==null?"Unknown":rootFolder.getPath()).append(",\n\t")
                .append("Volume: ").append(new DecimalFormat("#0.0").format(gain)).append(",\n\t")
                .append("Songs Count: ").append(songsCount).append(",\n\t")
                .append("Folder Count: ").append(foldersCount).append("\n}")
                .toString();
    }

}
