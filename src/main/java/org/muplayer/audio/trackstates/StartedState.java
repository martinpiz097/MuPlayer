package org.muplayer.audio.trackstates;

import org.muplayer.audio.Track;

public class StartedState extends TrackState {
    public StartedState(Track track) {
        super(track);
    }

    @Override
    public void handle() {
        track.play();
        finish();
    }
}