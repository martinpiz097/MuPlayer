package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.listener.notifier.TrackEventNotifier;

public class PausedState extends TrackState {
    public PausedState(Track track, TrackEventNotifier notifier) {
        super(track, TrackStateName.PAUSED, notifier);
    }

    @Override
    protected void handle() {
        synchronized (track) {
            try {
                track.wait();
            } catch (InterruptedException e) {
                track.finish();
            }
        }
    }
}