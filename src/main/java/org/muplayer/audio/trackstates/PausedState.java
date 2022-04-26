package org.muplayer.audio.trackstates;

import org.muplayer.audio.Track;

public class PausedState extends TrackState {
    public PausedState(Track track) {
        super(track);
    }

    // con thread.sleep tambien funciona porque se considera que lo ejecutado
    // aca esta dentro del run de Track
    @Override
    public void handle() {
        track.suspend();
    }
}