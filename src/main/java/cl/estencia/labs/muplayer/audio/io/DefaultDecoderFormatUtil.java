package cl.estencia.labs.muplayer.audio.io;

import cl.estencia.labs.aucom.audio.util.AudioDecodingUtil;

import javax.sound.sampled.AudioFormat;

public class DefaultAudioDecodingUtil extends AudioDecodingUtil {
    @Override
    public AudioFormat convertToPcmFormat(AudioFormat baseFormat) {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getFrameRate() * 1000,
                isSystemBigEndian());
    }

}
