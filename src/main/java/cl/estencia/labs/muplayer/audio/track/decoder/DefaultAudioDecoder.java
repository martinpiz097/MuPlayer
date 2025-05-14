package cl.estencia.labs.muplayer.audio.track.decoder;

import cl.estencia.labs.aucom.io.AudioDecoder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static cl.estencia.labs.muplayer.util.IOUtil.isSystemBigEndian;

public class DefaultAudioDecoder extends AudioDecoder {

    public DefaultAudioDecoder(String path) throws UnsupportedAudioFileException, IOException {
        super(path);
    }

    public DefaultAudioDecoder(File file) throws UnsupportedAudioFileException, IOException {
        super(file);
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

}
