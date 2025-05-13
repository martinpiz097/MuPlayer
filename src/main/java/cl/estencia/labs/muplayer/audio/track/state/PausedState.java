package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.listener.TrackEvent;

import java.util.List;

public class PausedState extends TrackState {
    public PausedState(Player player, Track track, List<TrackEvent> listInternalEvents) {
        super(player, track, TrackStateName.PAUSED, listInternalEvents);
    }

    @Override
    protected void handle() {
        synchronized (track) {
            try {
                track.wait();
            } catch (InterruptedException e) {
                track.kill();
            }
        }
    }
}