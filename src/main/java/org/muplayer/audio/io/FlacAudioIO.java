package org.muplayer.audio.io;

import javax.sound.sampled.AudioFormat;

public class FlacAudioIO extends AudioIO {
    @Override
    public AudioFormat getPcmFormat(AudioFormat baseFormat) {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                baseFormat.getSampleSizeInBits(),
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                //baseFormat.getFrameRate() == -1.0 ? baseFormat.getSampleRate() : baseFormat.getSampleRate() * 1000,
                baseFormat.getSampleRate(),
                baseFormat.isBigEndian());
    }
}
