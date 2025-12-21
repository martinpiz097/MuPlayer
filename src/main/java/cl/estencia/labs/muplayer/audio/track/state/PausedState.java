package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;
import cl.estencia.labs.muplayer.event.notifier.user.TrackUserEventNotifier;

public class PausedState extends TrackState {

    public PausedState(Track track) {
        super(TrackStateName.PAUSED, track);
    }

    @Override
    public void handle() {
        synchronized (track) {
            try {
                track.wait();
            } catch (InterruptedException e) {
                track.finish();
            }
        }
    }
}