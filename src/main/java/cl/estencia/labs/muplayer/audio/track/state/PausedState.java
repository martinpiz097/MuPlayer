package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.listener.TrackNotifier;

public class PausedState extends TrackState {
    public PausedState(Track track, TrackNotifier notifier) {
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