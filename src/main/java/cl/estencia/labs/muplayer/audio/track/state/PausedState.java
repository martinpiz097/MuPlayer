package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.listener.notifier.internal.TrackInternalEventNotifier;
import cl.estencia.labs.muplayer.listener.notifier.user.TrackUserEventNotifier;

public class PausedState extends TrackState {

    public PausedState(Track track,
                         TrackInternalEventNotifier internalEventNotifier,
                         TrackUserEventNotifier userEventNotifier) {
        super(TrackStateName.PAUSED, track, internalEventNotifier, userEventNotifier);
    }

    @Override
    public void handle() {
        sendStateEvent();
        synchronized (track) {
            try {
                track.wait();
            } catch (InterruptedException e) {
                track.finish();
            }
        }
    }
}