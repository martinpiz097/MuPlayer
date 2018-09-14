/*package org.muplayer.audio.trackstates;

import org.aucom.sound.Speaker;
import org.muplayer.audio.Track;

import javax.sound.sampled.AudioInputStream;

public class PausedState extends TrackState {

    private Track track;
    private AudioInputStream decodedStream;
    private Speaker trackLine;

    public PausedState(Track track) {
        this.track = track;
        decodedStream = track.getDecodedStream();
        trackLine = track.getTrackLine();
    }

    @Override
    public void handle() {
        while (track.isPaused()) {}
    }
}
*/
