package org.muplayer.audio.track.states;

import lombok.Data;
import lombok.extern.java.Log;
import org.muplayer.audio.track.Track;
import org.muplayer.audio.track.TrackData;
import org.muplayer.audio.track.TrackIO;

// podria mas adelante el estado ser un hilo
@Data
public abstract class TrackState {
    protected final Track track;
    protected volatile TrackData trackData;
    protected volatile TrackIO trackIO;
    protected volatile Runnable preTask;

    public TrackState(Track track) {
        this.track = track;
        this.trackData = track.getTrackData();
        this.trackIO = track.getTrackIO();
    }

    public String getName() {
        final String className = getClass().getSimpleName();
        return className.replace("State", "").trim();
    }

    public void execute() {
        if (preTask != null)
            preTask.run();
        handle();
    }

    public abstract void handle();
}