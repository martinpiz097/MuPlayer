package org.muplayer.model.trackstates;

import org.muplayer.audio.Track;

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
