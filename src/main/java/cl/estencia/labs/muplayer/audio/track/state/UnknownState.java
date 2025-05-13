package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.listener.TrackEvent;

import java.util.List;

// para evitar null
public class UnknownState extends TrackState {

    public UnknownState(Player player, Track track, List<TrackEvent> listInternalEvents) {
        super(player, track, TrackStateName.UNKNOWN, listInternalEvents);
    }

    @Override
    protected void handle() {

    }
}