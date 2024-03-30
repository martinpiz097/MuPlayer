package org.muplayer.audio.track.states;

import org.muplayer.audio.player.Player;
import org.muplayer.audio.track.Track;

public class KilledState extends TrackState {

    public KilledState(Player player, Track track) {
        super(player, track);
    }

    @Override
    public void handle() {
        trackIO.closeStream();
        trackIO.closeSpeaker();
        trackData.setCanTrackContinue(false);
    }
}
