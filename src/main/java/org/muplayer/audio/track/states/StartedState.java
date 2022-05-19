package org.muplayer.audio.track.states;

import org.muplayer.audio.track.Track;
import org.muplayer.audio.track.TrackData;

public class StartedState extends TrackState {
    public StartedState(Track track) {
        super(track);
    }

    @Override
    public void handle() {
        //Logger.getLogger(this, "SpeakerLine", track.getTrackIO().getTrackLine().getDriverInfo()).rawInfo();
        //if (track.getPlayerControl().isMute())
        //    track.mute();
        final TrackData trackData = track.getTrackData();
        if (trackData.isMute())
            track.mute();
        track.setGain(trackData.getGain());
        track.play();
        finish();
    }
}