package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.listener.TrackEvent;

import java.util.List;

public class StartedState extends TrackState {
    public StartedState(Player player, Track track, List<TrackEvent> listInternalEvents) {
        super(player, track, TrackStateName.STARTED, listInternalEvents);
    }

    @Override
    protected void handle() {
        if (trackData.isMute()) {
            track.mute();
        }
        track.setVolume(trackData.getVolume());
        track.play();
    }
}