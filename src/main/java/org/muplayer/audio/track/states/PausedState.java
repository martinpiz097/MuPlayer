package org.muplayer.audio.track.states;

import org.muplayer.audio.player.Player;
import org.muplayer.audio.track.Track;

public class PausedState extends TrackState {
    public PausedState(Player player, Track track) {
        super(player, track);
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