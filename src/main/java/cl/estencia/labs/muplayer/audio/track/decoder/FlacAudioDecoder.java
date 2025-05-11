package cl.estencia.labs.muplayer.audio.track.decoder;

import cl.estencia.labs.aucom.io.AudioDecoder;
import cl.estencia.labs.muplayer.audio.track.decoder.util.FlacDecoderFormatUtil;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class FlacAudioDecoder extends AudioDecoder {

    public FlacAudioDecoder(String path) throws UnsupportedAudioFileException, IOException {
        this(new File(path));
    }

    public FlacAudioDecoder(File file) throws UnsupportedAudioFileException, IOException {
        super(file, new FlacDecoderFormatUtil());
    }

    @Override
    public AudioInputStream getDecodedStream() {
        return decoderFormatUtil.decodeToPcm(sourceStream);
    }
}
