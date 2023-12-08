package org.muplayer.audio.track.states;

import org.muplayer.audio.track.Track;
import org.muplayer.audio.track.TrackIO;
import org.muplayer.util.AudioUtil;

public class PausedState extends TrackState {
    public PausedState(Track track) {
        super(track);
    }

    @Override
    public void handle() {
        synchronized (track) {
            try {
                track.wait();
            } catch (InterruptedException e) {
                track.kill();
            }
        }
    }
}