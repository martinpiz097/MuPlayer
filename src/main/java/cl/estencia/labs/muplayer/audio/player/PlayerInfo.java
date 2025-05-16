package cl.estencia.labs.muplayer.audio.player;

import cl.estencia.labs.muplayer.audio.track.Track;
import lombok.Data;

import java.io.File;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class PlayerInfo {
    private final AtomicReference<Track> current;
    private final File trackFolder;
    private final File rootFolder;
    private final float volume;
    private final int songsCount;
    private final int foldersCount;

    private static final String ENTER_TAB = ",\n\t";

    public PlayerInfo(MuPlayer muPlayer) {
        this.current = muPlayer.getCurrentTrack();
        trackFolder = current.get() != null && current.get().getDataSource() != null
                ? current.get().getDataSource().getParentFile() : null;
        rootFolder = muPlayer.getRootFolder();
        volume = muPlayer.getVolume();
        songsCount = muPlayer.getSongsCount();
        foldersCount = muPlayer.getFoldersCount();
    }

    public String getCurrentTrackTitle() {
        return current.get() == null
                ? "Title Unknown"
                : current.get().getTitle();
    }

    public String getCurrentTrackFormat() {
        if (current.get() == null)
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
