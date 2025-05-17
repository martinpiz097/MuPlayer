package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;
import cl.estencia.labs.muplayer.event.notifier.user.TrackUserEventNotifier;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.logging.Level;

@Log
public class PlayingState extends TrackState {

    public PlayingState(Track track,
                         TrackInternalEventNotifier internalEventNotifier,
                         TrackUserEventNotifier userEventNotifier) {
        super(TrackStateName.PLAYING, track, internalEventNotifier, userEventNotifier);
    }

    @Override
    public void handle() {
        try {
            // ver si puedo reproducir quitando el while
            while (track.isPlaying()) {
                speaker.playAudioStream(decodedAudioStream);
                sendStateEvent();
            }
        } catch (IOException | IndexOutOfBoundsException | IllegalArgumentException e) {
            log.log(Level.SEVERE, "Error on playing sound " + track.getTitle() + ": ", e);
        }

        track.finish();
    }

}
