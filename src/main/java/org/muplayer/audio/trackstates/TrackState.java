package org.muplayer.audio.trackstates;

import org.muplayer.audio.Track;

// podria mas adelante el estado ser un hilo
public abstract class TrackState {

    protected final Track track;
    protected volatile boolean canTrackContinue;

    public TrackState(Track track) {
        this.track = track;
        canTrackContinue = true;
    }

    public boolean canTrackContinue() {
        return canTrackContinue;
    }

    public String getName() {
        final String className = getClass().getSimpleName();
        return className.replace("State", "").trim();
    }
    public abstract void handle();
    public void finish() {
        canTrackContinue = false;
    }
}