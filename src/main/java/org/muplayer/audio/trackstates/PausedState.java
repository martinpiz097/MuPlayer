package org.muplayer.audio.trackstates;

import org.muplayer.audio.Track;

public class PausedState extends TrackState {
    public PausedState(Track track) {
        super(track);
    }

    @Override
    public void handle() {
        track.suspend();
    }
}