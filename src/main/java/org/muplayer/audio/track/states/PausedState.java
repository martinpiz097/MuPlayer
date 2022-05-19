package org.muplayer.audio.track.states;

import org.muplayer.audio.track.Track;
import org.muplayer.audio.track.TrackIO;
import org.muplayer.util.AudioUtil;

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