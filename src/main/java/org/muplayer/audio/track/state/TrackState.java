package org.muplayer.audio.track.state;

import lombok.Data;
import org.muplayer.audio.player.Player;
import org.muplayer.audio.track.Track;
import org.muplayer.audio.track.TrackData;
import org.muplayer.audio.track.TrackIO;

@Data
public abstract class TrackState {
    protected final Player player;
    protected final Track track;
    protected volatile TrackData trackData;
    protected volatile TrackIO trackIO;

    public TrackState(Player player, Track track) {
        this.player = player;
        this.track = track;
        this.trackData = track.getTrackData();
        this.trackIO = track.getTrackIO();
    }

    public String getName() {
        final String className = getClass().getSimpleName();
        return className.replace("State", "").trim();
    }

    public abstract void handle();
}