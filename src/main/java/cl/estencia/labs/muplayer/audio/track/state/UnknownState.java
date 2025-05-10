package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;

public class UnknownState extends TrackState {

    public UnknownState(Player player, Track track) {
        super(player, track);
    }

    @Override
    public void handle() {

    }
}