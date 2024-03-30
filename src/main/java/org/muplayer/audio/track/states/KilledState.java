package org.muplayer.audio.track.states;

import org.muplayer.audio.player.Player;
import org.muplayer.audio.track.Track;

public class KilledState extends TrackState {
    private final Player player;

    public KilledState(Track track, Player player) {
        super(track);
        this.player = player;
    }

    @Override
    public void handle() {
        trackIO.closeStream();
        trackIO.closeSpeaker();
        trackData.setCanTrackContinue(false);
    }
}
