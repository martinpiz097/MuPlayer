package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.listener.notifier.internal.TrackInternalEventNotifier;
import cl.estencia.labs.muplayer.listener.notifier.user.TrackUserEventNotifier;

public class UnknownState extends TrackState {

    public UnknownState(Track track,
                         TrackInternalEventNotifier internalEventNotifier,
                         TrackUserEventNotifier userEventNotifier) {
        super(TrackStateName.UNKNOWN, track, internalEventNotifier, userEventNotifier);
    }

    @Override
    public void handle() {
        sendStateEvent();
    }
}