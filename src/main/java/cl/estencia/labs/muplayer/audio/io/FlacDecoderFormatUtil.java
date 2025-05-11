package cl.estencia.labs.muplayer.audio.io;

import cl.estencia.labs.aucom.audio.util.AudioDecodingUtil;

import javax.sound.sampled.AudioFormat;

public class FlacAudioDecodingUtil extends AudioDecodingUtil {
    @Override
    public AudioFormat convertToPcmFormat(AudioFormat baseFormat) {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                baseFormat.getSampleSizeInBits(),
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                //baseFormat.getFrameRate() == -1.0 ? baseFormat.getSampleRate() : baseFormat.getSampleRate() * 1000,
                baseFormat.getSampleRate(),
                isSystemBigEndian());
    }
}
