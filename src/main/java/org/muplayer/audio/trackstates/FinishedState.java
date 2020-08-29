package org.muplayer.audio.trackstates;

import org.muplayer.audio.Track;
import org.muplayer.audio.interfaces.PlayerControls;

public class FinishedState extends TrackState {

    private final PlayerControls player;

    public FinishedState(Track track) {
        super(track);
        player = track.getPlayer();
    }

    @Override
    public void handle() {
        if (player != null)
            player.playNext();
    }
}