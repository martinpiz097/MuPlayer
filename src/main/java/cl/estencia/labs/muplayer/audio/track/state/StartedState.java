package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;
import cl.estencia.labs.muplayer.event.notifier.user.TrackUserEventNotifier;

public class StartedState extends TrackState {

    public StartedState(Track track) {
        super(TrackStateName.STARTED, track);
    }

    @Override
    public void handle() {
        speaker.open();

        if (trackStatusData.isMute()) {
            track.mute();
        }
        track.setVolume(trackStatusData.getVolume());
        track.play();
    }
}