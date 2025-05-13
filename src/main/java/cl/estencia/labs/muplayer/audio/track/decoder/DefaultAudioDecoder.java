package cl.estencia.labs.muplayer.audio.track.decoder;

import cl.estencia.labs.aucom.io.AudioDecoder;
import cl.estencia.labs.muplayer.audio.track.decoder.util.DefaultDecoderFormatUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static cl.estencia.labs.muplayer.util.IOUtil.isSystemBigEndian;

public class DefaultAudioDecoder extends AudioDecoder {

    public DefaultAudioDecoder(String path) throws UnsupportedAudioFileException, IOException {
        this(new File(path));
    }

    public DefaultAudioDecoder(File file) throws UnsupportedAudioFileException, IOException {
        super(file, new DefaultDecoderFormatUtil());
    }

    @Override
    protected AudioFormat convertToPcmFormat(AudioFormat baseFormat) {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getFrameRate() * 1000,
                isSystemBigEndian());
    }

    @Override
    public AudioInputStream getDecodedStream() {
        AudioFormat baseFormat = getBaseFormat();
        AudioFormat pcmFormat = convertToPcmFormat(baseFormat);

        return decoderFormatUtil.decodeToPcm(sourceStream, pcmFormat);
    }
}
