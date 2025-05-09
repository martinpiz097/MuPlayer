package cl.estencia.labs.muplayer.muplayer.audio.track.state;

import lombok.Data;
import cl.estencia.labs.muplayer.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.muplayer.audio.track.TrackStatusData;
import cl.estencia.labs.muplayer.muplayer.audio.track.TrackIO;

@Data
public abstract class TrackState {
    protected final Player player;
    protected final Track track;
    protected final TrackStatusData trackData;
    protected volatile TrackIO trackIO;

    public TrackState(Player player, Track track) {
        this.player = player;
        this.track = track;
        this.trackData = track.getTrackStatusData();
        this.trackIO = track.getTrackIO();
    }

    public String getName() {
        final String className = getClass().getSimpleName();
        return className.replace("State", "").trim();
    }

    public abstract void handle();
}