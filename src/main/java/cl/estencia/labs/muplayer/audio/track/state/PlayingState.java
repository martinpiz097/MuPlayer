package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;
import cl.estencia.labs.muplayer.event.notifier.user.TrackUserEventNotifier;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.logging.Level;

import static cl.estencia.labs.aucom.common.IOConstants.DEFAULT_BUFF_SIZE;
import static cl.estencia.labs.aucom.common.IOConstants.EOF;

@Log
public class PlayingState extends TrackState {

    public PlayingState(Track track) {
        super(TrackStateName.PLAYING, track);
    }

    @Override
    public void handle() {
        try {
            byte[] audioBuffer = new byte[DEFAULT_BUFF_SIZE];
            int read;
            while (track.isPlaying() && (read = decodedAudioStream.read(audioBuffer)) != EOF) {
                speaker.playAudio(audioBuffer, read);
            }
        } catch (IOException | IndexOutOfBoundsException | IllegalArgumentException e) {
            log.log(Level.SEVERE, "Error on playing sound " + track.getTitle() + ": ", e);
        }

        if (track.isActive()) {
            track.finish();
        }
    }

}
