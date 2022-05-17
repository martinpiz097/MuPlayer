package org.muplayer.audio.track.states;

import org.muplayer.audio.track.Track;

public class KilledState extends TrackState {

    public KilledState(Track track) {
        super(track);
    }

    @Override
    public void handle() {
        track.closeAllStreams();
        finish();
    }
}
