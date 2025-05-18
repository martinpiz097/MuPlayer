package cl.estencia.labs.muplayer.audio.track.decoder;

import cl.estencia.labs.aucom.core.io.AudioDecoder;
import cl.estencia.labs.muplayer.core.util.IOUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class FlacAudioDecoder extends AudioDecoder {

    public FlacAudioDecoder(String path) throws UnsupportedAudioFileException, IOException {
        super(path);
    }

    public FlacAudioDecoder(File file) throws UnsupportedAudioFileException, IOException {
        super(file);
    }

    @Override
    public AudioFormat convertToPcmFormat(AudioFormat baseFormat) {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                baseFormat.getSampleSizeInBits(),
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                //baseFormat.getFrameRate() == -1.0 ? baseFormat.getSampleRate() : baseFormat.getSampleRate() * 1000,
                baseFormat.getSampleRate(),
                IOUtil.isSystemBigEndian());
    }

}
