package org.muplayer.audio.trackstates;

import org.muplayer.audio.Track;
import org.muplayer.interfaces.Player;

public class FinishedState extends TrackState {

    private final Player player;

    public FinishedState(Track track) {
        super(track);
        player = track.getPlayerControl();
    }

    @Override
    public void handle() {
        track.closeAllStreams();
        if (player != null)
            player.playNext();
    }
}