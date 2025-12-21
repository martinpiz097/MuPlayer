package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;
import cl.estencia.labs.muplayer.event.notifier.user.TrackUserEventNotifier;

public class UnknownState extends TrackState {

    public UnknownState(Track track) {
        super(TrackStateName.UNKNOWN, track);
    }

    @Override
    public void handle() {
    }

}