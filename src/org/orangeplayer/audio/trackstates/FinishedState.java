/*package org.orangeplayer.audio.trackstates;

import org.aucom.sound.Speaker;
import org.orangeplayer.audio.Track;

import javax.sound.sampled.AudioInputStream;

public class FinishedState implements TrackState {

    private Track track;
    private AudioInputStream speakerAis;
    private Speaker trackLine;

    public FinishedState(Track track) {
        this.track = track;
        speakerAis = track.getTrackStream();
        trackLine = track.getTrackLine();
    }

    @Override
    public void handle() {
        track.closeAll();
    }
}
*/