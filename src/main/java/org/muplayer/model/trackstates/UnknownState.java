package org.muplayer.model.trackstates;

import org.muplayer.audio.Track;

public class UnknownState extends TrackState {

    public UnknownState(Track track) {
        super(track);
    }

    @Override
    public void handle() {

    }
}