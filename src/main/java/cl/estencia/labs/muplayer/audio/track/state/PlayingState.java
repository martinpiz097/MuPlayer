package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static cl.estencia.labs.muplayer.core.aucom.common.IOConstants.DEFAULT_BUFF_SIZE;
import static cl.estencia.labs.muplayer.core.aucom.common.IOConstants.EOF;

@Slf4j
public class PlayingState extends TrackState {

    public PlayingState(Track track) {
        super(TrackStateName.PLAYING, track);
    }

    @Override
    public void handle() {
        try {
            // TODO arreglar el goto y el seek, cagaron de nuevo
            final byte[] audioBuffer = new byte[DEFAULT_BUFF_SIZE];
            int read;
            // track.isPlaying() necesario, sino la pista se queda reproduciendo aunque
            // pase a la siguiente manualmente o se cambie el estado
            while (track.isPlaying() && (read = decodedAudioStream.read(audioBuffer)) != EOF) {
                speaker.playAudio(audioBuffer, read);
            }
        } catch (Exception e) {
            log.error("Error on playing sound " + track.getTitle() + ": ", e);
        }

        // if para finalizar cancion cuando hay errores, desde el reproductir se suele usar kill
        // en vez de finish para finalizar la cancion antes de cambiarla por otra
        if (track.isActive() && !track.isSuspended() && !track.isReverberating()) {
            track.finish();
        }
    }

}
