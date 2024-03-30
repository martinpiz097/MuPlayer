package org.muplayer.audio.track.states;

import lombok.extern.java.Log;
import org.muplayer.audio.track.Track;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.logging.Level;

@Log
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
            synchronized (track) {
                track.resetStream();
                track.getTrackData().setSecsSeeked(0);
                track.wait();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage());
            track.kill();
        }
    }
}