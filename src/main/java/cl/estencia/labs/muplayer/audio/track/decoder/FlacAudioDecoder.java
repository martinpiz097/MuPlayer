package cl.estencia.labs.muplayer.audio.track.decoder;

import cl.estencia.labs.aucom.io.AudioDecoder;
import cl.estencia.labs.muplayer.audio.track.decoder.util.FlacDecoderFormatUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static cl.estencia.labs.muplayer.util.IOUtil.isSystemBigEndian;

public class FlacAudioDecoder extends AudioDecoder {

    public FlacAudioDecoder(String path) throws UnsupportedAudioFileException, IOException {
        this(new File(path));
    }

    public FlacAudioDecoder(File file) throws UnsupportedAudioFileException, IOException {
        super(file, new FlacDecoderFormatUtil());
    }

    @Override
    protected AudioFormat convertToPcmFormat(AudioFormat baseFormat) {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                baseFormat.getSampleSizeInBits(),
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                //baseFormat.getFrameRate() == -1.0 ? baseFormat.getSampleRate() : baseFormat.getSampleRate() * 1000,
                baseFormat.getSampleRate(),
                isSystemBigEndian());
    }

    @Override
    public AudioInputStream getDecodedStream() {
        AudioFormat baseFormat = getBaseFormat();
        AudioFormat pcmFormat = convertToPcmFormat(baseFormat);

        return decoderFormatUtil.decodeToPcm(sourceStream, pcmFormat);
    }
}
