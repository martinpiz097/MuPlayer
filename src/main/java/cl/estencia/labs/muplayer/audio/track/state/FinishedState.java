package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.listener.notifier.TrackEventNotifier;

public class FinishedState extends TrackState {

    public FinishedState(Track track, TrackEventNotifier notifier) {
        super(track, TrackStateName.FINISHED, notifier);
    }

    @Override
    protected void handle() {
        trackIOUtil.closeStream(decodedAudioStream);
        speaker.close();
        trackData.setCanTrackContinue(false);

        notifier.shutdown();
    }
}
