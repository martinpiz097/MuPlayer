package org.muplayer.audio.track.states;

import org.muplayer.audio.track.Track;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class StoppedState extends TrackState {

    public StoppedState(Track track) {
        super(track);
    }

    private void resetStream() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        track.resetStream();
    }

    @Override
    public void handle() {
        try {
            track.resetStream();
            track.getTrackData().setSecsSeeked(0);
            synchronized (track) {
                track.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}