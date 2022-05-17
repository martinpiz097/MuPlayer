package org.muplayer.audio.track.states;

import org.muplayer.audio.track.Track;

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