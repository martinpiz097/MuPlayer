package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.listener.TrackNotifier;
import cl.estencia.labs.muplayer.audio.track.listener.TrackStateListener;

import java.util.List;

// para evitar null
public class UnknownState extends TrackState {

    public UnknownState(Track track, TrackNotifier notifier) {
        super(track, TrackStateName.UNKNOWN, notifier);
    }

    @Override
    protected void handle() {

    }
}