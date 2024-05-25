package org.muplayer.audio.io;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.nio.ByteOrder;

public class DefaultAudioIO extends AudioIO {
    @Override
    public AudioFormat getPcmFormat(AudioFormat baseFormat) {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getFrameRate() * 1000,
                isSystemBigEndian());
    }

}
