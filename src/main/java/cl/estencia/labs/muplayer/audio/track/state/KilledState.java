package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.listener.TrackEvent;

import java.util.List;

public class KilledState extends TrackState {

    public KilledState(Player player, Track track, List<TrackEvent> listInternalEvents) {
        super(player, track, TrackStateName.KILLED, listInternalEvents);
    }

    @Override
    protected void handle() {
        trackIO.closeStream();
        trackIO.closeSpeaker();
        trackData.setCanTrackContinue(false);
    }
}
