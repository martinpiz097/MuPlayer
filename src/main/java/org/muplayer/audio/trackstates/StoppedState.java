package org.muplayer.audio.trackstates;

import org.muplayer.audio.Track;

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
            track.setSecsSeeked(0);
            track.suspend();
            System.out.println();
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
}