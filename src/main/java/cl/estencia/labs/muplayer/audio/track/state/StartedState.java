package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.listener.TrackNotifier;

public class StartedState extends TrackState {
    public StartedState(Track track, TrackNotifier notifier) {
        super(track, TrackStateName.STARTED, notifier);
    }

    @Override
    protected void handle() {
        if (notifier.getState() == Thread.State.NEW) {
            notifier.start();
        }

        if (trackData.isMute()) {
            track.mute();
        }
        track.setVolume(trackData.getVolume());
        track.play();
    }
}