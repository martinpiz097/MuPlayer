package cl.estencia.labs.muplayer.audio.player;

import lombok.Data;
import cl.estencia.labs.muplayer.audio.track.Track;

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

    private static final String ENTER_TAB = ",\n\t";

    public PlayerInfo(MuPlayer muPlayer) {
        this.current = muPlayer.getCurrent();
        trackFolder = current != null && current.getDataSource() != null
                ? current.getDataSource().getParentFile() : null;
        rootFolder = muPlayer.getRootFolder();
        volume = muPlayer.getVolume();
        songsCount = muPlayer.getSongsCount();
        foldersCount = muPlayer.getFoldersCount();
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
                .append("Track: ").append(getCurrentTrackTitle()).append(ENTER_TAB)
                .append("Track Format: ").append(getCurrentTrackFormat()).append(ENTER_TAB)
                .append("Track Folder: ").append(trackFolder==null?"Unknown":trackFolder.getPath()).append(ENTER_TAB)
                .append("Root Folder: ").append(rootFolder==null?"Unknown":rootFolder.getPath()).append(ENTER_TAB)
                .append("Volume: ").append(new DecimalFormat("#0.0").format(volume)).append(ENTER_TAB)
                .append("Songs Count: ").append(songsCount).append(ENTER_TAB)
                .append("Folder Count: ").append(foldersCount).append("\n}")
                .toString();
    }

}
