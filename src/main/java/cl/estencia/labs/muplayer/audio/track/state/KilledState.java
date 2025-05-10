package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;

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
