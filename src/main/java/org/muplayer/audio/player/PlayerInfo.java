package org.muplayer.audio.player;

import lombok.Data;
import org.muplayer.audio.track.Track;

import java.io.File;
import java.text.DecimalFormat;

@Data
public class PlayerInfo {
    private final Track current;
    private final File trackFolder;
    private final File rootFolder;
    private final float volume;

    private final int songsCount;
    private final int foldersCount;

    public PlayerInfo(MusicPlayer musicPlayer) {
        this.current = musicPlayer.getCurrent();
        trackFolder = current != null && current.getDataSource() != null
                ? current.getDataSource().getParentFile() : null;
        rootFolder = musicPlayer.getRootFolder();
        volume = musicPlayer.getVolume();
        songsCount = musicPlayer.getSongsCount();
        foldersCount = musicPlayer.getFoldersCount();
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

    @Override
    public String toString() {
        return new StringBuilder().append("{\n\t")
                .append("Track: ").append(getCurrentTrackTitle()).append(",\n\t")
                .append("Track Format: ").append(getCurrentTrackFormat()).append(",\n\t")
                .append("Track Folder: ").append(trackFolder==null?"Unknown":trackFolder.getPath()).append(",\n\t")
                .append("Root Folder: ").append(rootFolder==null?"Unknown":rootFolder.getPath()).append(",\n\t")
                .append("Volume: ").append(new DecimalFormat("#0.0").format(volume)).append(",\n\t")
                .append("Songs Count: ").append(songsCount).append(",\n\t")
                .append("Folder Count: ").append(foldersCount).append("\n}")
                .toString();
    }

}
