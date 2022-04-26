package org.muplayer.audio.trackstates;

import org.muplayer.audio.Track;
import org.muplayer.interfaces.PlayerControls;

public class FinishedState extends TrackState {

    private final PlayerControls player;

    public FinishedState(Track track) {
        super(track);
        player = track.getPlayer();
    }

    @Override
    public void handle() {
        track.closeAllStreams();
        if (player != null)
            player.playNext();
    }
}