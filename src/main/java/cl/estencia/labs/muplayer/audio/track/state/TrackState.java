package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.aucom.audio.device.Speaker;
import lombok.Data;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.TrackStatusData;
import cl.estencia.labs.muplayer.audio.track.TrackIO;
import lombok.EqualsAndHashCode;

public abstract class TrackState {
    protected final Player player;
    protected final Track track;
    protected final TrackStatusData trackData;
    protected final TrackIO trackIO;
    protected final Speaker speaker;

    public TrackState(Player player, Track track) {
        this.player = player;
        this.track = track;
        this.trackData = track.getTrackStatusData();
        this.trackIO = track.getTrackIO();
        this.speaker = track.getSpeaker();
    }

    public String getName() {
        final String className = getClass().getSimpleName();
        return className.replace("State", "").trim();
    }

    public abstract void handle();
}