package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;
import cl.estencia.labs.muplayer.event.notifier.user.TrackUserEventNotifier;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class ReverberatedState extends TrackState {
    private final double seekSeconds;

    public ReverberatedState(Track track,
                             TrackInternalEventNotifier internalEventNotifier,
                             TrackUserEventNotifier userEventNotifier, double seekSeconds) {
        super(TrackStateName.REVERBERATED, track, internalEventNotifier, userEventNotifier);
        this.seekSeconds = seekSeconds;
    }

    @Override
    public void handle() {
        try {
            sendStateEvent();
            track.resetStream();
            track.getTrackStatusData().setSecsSeeked(0.0d);
            track.seek(Math.max(seekSeconds, 0));
            track.play();
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
}
