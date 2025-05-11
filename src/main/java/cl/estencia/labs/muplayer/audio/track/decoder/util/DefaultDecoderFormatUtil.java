package cl.estencia.labs.muplayer.audio.io;

import cl.estencia.labs.aucom.util.DecoderFormatUtil;

import javax.sound.sampled.AudioFormat;

public class DefaultDecoderFormatUtil extends DecoderFormatUtil {
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
