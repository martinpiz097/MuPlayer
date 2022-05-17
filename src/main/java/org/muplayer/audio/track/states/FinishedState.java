package org.muplayer.audio.track.states;

import org.muplayer.audio.track.Track;
import org.muplayer.audio.player.Player;

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