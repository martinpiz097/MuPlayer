package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;
import cl.estencia.labs.muplayer.event.notifier.user.TrackUserEventNotifier;

public class StartedState extends TrackState {

    public StartedState(Track track,
                         TrackInternalEventNotifier internalEventNotifier,
                         TrackUserEventNotifier userEventNotifier) {
        super(TrackStateName.STARTED, track, internalEventNotifier, userEventNotifier);
    }

    @Override
    public void handle() {
        speaker.open();

        sendStateEvent();
        internalEventNotifier.start();
        userEventNotifier.start();

        if (trackStatusData.isMute()) {
            track.mute();
        }
        track.setVolume(trackStatusData.getVolume());
        track.play();
    }
}