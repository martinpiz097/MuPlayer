package org.muplayer.model.trackstates;

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