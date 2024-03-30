package org.muplayer.audio.track.states;

import org.muplayer.audio.player.Player;
import org.muplayer.audio.track.Track;

public class UnknownState extends TrackState {

    public UnknownState(Player player, Track track) {
        super(player, track);
    }

    @Override
    public void handle() {

    }
}