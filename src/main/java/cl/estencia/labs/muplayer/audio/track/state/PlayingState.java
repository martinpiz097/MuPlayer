package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.listener.notifier.TrackEventNotifier;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.logging.Level;

import static cl.estencia.labs.aucom.common.IOConstants.DEFAULT_BUFF_SIZE;
import static cl.estencia.labs.aucom.common.IOConstants.EOF;

@Log
public class PlayingState extends TrackState {
    private final byte[] audioBuffer;

    public PlayingState(Track track, TrackEventNotifier notifier) {
        super(track, TrackStateName.PLAYING, notifier);
        this.audioBuffer = new byte[DEFAULT_BUFF_SIZE];
    }

    private int readNextBytes() throws IOException {
        return decodedAudioStream.read(audioBuffer);
    }

    @Override
    protected void handle() {
        try {
            int read;
            while (track.isPlaying() && (read = readNextBytes()) != EOF) {
                speaker.playAudio(audioBuffer, read);
            }
        } catch (IOException | IndexOutOfBoundsException | IllegalArgumentException e) {
            log.log(Level.SEVERE, "Error on playing sound " + track.getTitle() + ": ", e);
        }

        track.finish();
    }

}
