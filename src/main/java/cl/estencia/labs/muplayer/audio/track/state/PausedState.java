package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.listener.TrackNotifier;
import cl.estencia.labs.muplayer.audio.track.listener.TrackStateListener;

import java.util.List;

public class PausedState extends TrackState {
    public PausedState(Player player, Track track, TrackNotifier notifier) {
        super(player, track, TrackStateName.PAUSED, notifier);
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