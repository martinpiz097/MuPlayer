/*package org.muplayer.audio.trackstates;

import org.aucom.sound.Speaker;
import org.muplayer.audio.Track;

import javax.sound.sampled.AudioInputStream;

public class FinishedState implements TrackState {

    private Track track;
    private AudioInputStream trackStream;
    private Speaker trackLine;

    public FinishedState(Track track) {
        this.track = track;
        trackStream = track.getTrackStream();
        trackLine = track.getTrackLine();
    }

    @Override
    public void handle() {
        track.closeAll();
    }
}
*/