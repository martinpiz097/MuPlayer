package org.muplayer.audio.track.states;

import org.muplayer.audio.track.Track;

// podria mas adelante el estado ser un hilo
public abstract class TrackState {

    protected final Track track;
    protected volatile boolean canTrackContinue;
    protected volatile Runnable preTask;

    public TrackState(Track track) {
        this.track = track;
        canTrackContinue = true;
    }

    public boolean canTrackContinue() {
        return canTrackContinue;
    }

    public Runnable getPreTask() {
        return preTask;
    }

    public void setPreTask(Runnable preTask) {
        this.preTask = preTask;
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
    public synchronized void finish() {
        canTrackContinue = false;
    }
}