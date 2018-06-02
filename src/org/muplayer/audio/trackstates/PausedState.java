/*package org.muplayer.audio.trackstates;

import org.aucom.sound.Speaker;
import org.muplayer.audio.Track;

import javax.sound.sampled.AudioInputStream;

public class PausedState implements TrackState {

    private Track track;
    private AudioInputStream trackStream;
    private Speaker trackLine;

    public PausedState(Track track) {
        this.track = track;
        trackStream = track.getTrackStream();
        trackLine = track.getTrackLine();
    }

    @Override
    public void handle() {
        while (true) {}
    }
}
*/