package cl.estencia.labs.muplayer.audio.track.decoder;

import cl.estencia.labs.aucom.io.AudioDecoder;
import cl.estencia.labs.aucom.util.DecoderFormatUtil;
import cl.estencia.labs.muplayer.audio.io.DefaultDecoderFormatUtil;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class DefaultAudioDecoder extends AudioDecoder {

    public DefaultAudioDecoder(String path) throws UnsupportedAudioFileException, IOException {
        this(new File(path));
    }

    public DefaultAudioDecoder(File file) throws UnsupportedAudioFileException, IOException {
        super(file, new DefaultDecoderFormatUtil());
    }

    @Override
    public AudioInputStream getDecodedStream() {
        return decoderFormatUtil.decodeToPcm(sourceStream);
    }
}
