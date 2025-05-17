package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;
import cl.estencia.labs.muplayer.event.notifier.user.TrackUserEventNotifier;

import java.util.concurrent.CompletableFuture;

public class FinishedState extends TrackState {

    public FinishedState(Track track,
                         TrackInternalEventNotifier internalEventNotifier,
                         TrackUserEventNotifier userEventNotifier) {
        super(TrackStateName.FINISHED, track, internalEventNotifier, userEventNotifier);
    }

    @Override
    public void handle() {
        trackIOUtil.closeStream(decodedAudioStream);
        speaker.close();
        trackStatusData.setCanTrackContinue(false);

        sendStateEvent();
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(500);
                internalEventNotifier.shutdown();
                userEventNotifier.shutdown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
