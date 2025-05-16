package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.listener.notifier.TrackEventNotifier;

// para evitar null
public class UnknownState extends TrackState {

    public UnknownState(Track track, TrackEventNotifier notifier) {
        super(track, TrackStateName.UNKNOWN, notifier);
    }

    @Override
    protected void handle() {

    }
}