package org.muplayer.audio.track.states;

import lombok.extern.java.Log;
import org.muplayer.audio.track.Track;

import java.util.logging.Level;

@Log
public class ReloadedState extends TrackState {
    public ReloadedState(Track track) {
        super(track);
    }

    @Override
    public void handle() {
        try {
            synchronized (track) {
                trackIO.closeStream();
                trackIO.closeSpeaker();
            }
            track.setTrackState(new InitializedState(track));
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage());
            track.kill();
        }
    }
}
