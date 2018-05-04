/*package org.orangeplayer.audio.trackstates;

import org.aucom.sound.Speaker;
import org.orangeplayer.audio.Track;

import javax.sound.sampled.AudioInputStream;

public class PausedState implements TrackState {

    private Track track;
    private AudioInputStream speakerAis;
    private Speaker trackLine;

    public PausedState(Track track) {
        this.track = track;
        speakerAis = track.getTrackStream();
        trackLine = track.getTrackLine();
    }

    @Override
    public void handle() {
        while (true) {}
    }
}
*/