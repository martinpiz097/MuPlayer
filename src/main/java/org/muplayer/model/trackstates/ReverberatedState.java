package org.muplayer.model.trackstates;

import org.muplayer.audio.Track;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class ReverberatedState extends TrackState {

    private final double seekSeconds;

    public ReverberatedState(Track track, double seekSeconds) {
        super(track);
        this.seekSeconds = seekSeconds;
    }

    @Override
    public void handle() {
        try {
            track.resetStream();
            track.getTrackData().setSecsSeeked(0);
            track.seek(Math.max(seekSeconds, 0));
            track.play();
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
}
