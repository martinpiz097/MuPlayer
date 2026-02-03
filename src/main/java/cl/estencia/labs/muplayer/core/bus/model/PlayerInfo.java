package cl.estencia.labs.muplayer.core.bus.model;

import cl.estencia.labs.muplayer.audio.model.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.track.Track;
import lombok.Getter;

import java.io.File;
import java.util.List;

@Getter
public class PlayerInfo {
    private final Track currentTrack;
    private final File currentFolder;
    private final List<Track> tracks;
    private final List<File> folders;
    private final PlayerStatusData playerStatusData;

    public PlayerInfo(Track currentTrack, List<Track> tracks, List<File> folders, PlayerStatusData playerStatusData) {
        this.currentTrack = currentTrack;
        this.currentFolder = currentTrack != null ? currentTrack.getDataSource() : null;
        this.tracks = tracks;
        this.folders = folders;
        this.playerStatusData = playerStatusData;
    }

}
