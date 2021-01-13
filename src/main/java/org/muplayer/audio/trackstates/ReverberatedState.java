package org.muplayer.audio.trackstates;

import org.muplayer.audio.Track;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class ReverberatedState extends TrackState {

    private final double second;

    public ReverberatedState(Track track, double second) {
        super(track);
        this.second = second;
    }

    @Override
    public void handle() {
        try {
            track.resetStream();
            track.setSecsSeeked(0);
            track.seek(Math.max(second, 0));
            track.play();
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
}
