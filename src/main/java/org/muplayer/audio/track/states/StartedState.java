package org.muplayer.audio.track.states;

import org.muplayer.audio.track.Track;
import org.muplayer.audio.track.TrackData;

public class StartedState extends TrackState {
    private final TrackData trackData;

    public StartedState(Track track) {
        super(track);
        this.trackData = track.getTrackData();
    }

    @Override
    public void handle() {
        if (trackData.isMute())
            track.mute();
        track.setVolume(trackData.getVolume());
        track.play();
        finish();
    }
}