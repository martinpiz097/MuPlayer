package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;

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