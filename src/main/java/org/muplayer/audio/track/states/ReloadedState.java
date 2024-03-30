package org.muplayer.audio.track.states;

import lombok.extern.java.Log;
import org.muplayer.audio.player.Player;
import org.muplayer.audio.track.Track;

import java.util.logging.Level;

@Log
public class ReloadedState extends TrackState {
    public ReloadedState(Player player, Track track) {
        super(player, track);
    }

    @Override
    public void handle() {
        try {
            trackIO.closeStream();
            trackIO.closeSpeaker();
            track.setTrackState(new InitializedState(player, track));
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage());
            track.kill();
        }
    }
}
