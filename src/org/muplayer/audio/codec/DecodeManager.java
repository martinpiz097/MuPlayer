package org.muplayer.audio.codec;

import org.tritonus.sampled.file.jorbis.JorbisAudioFileReader;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DecodeManager {

    public static boolean isVorbis(File fSound) {
        try {
            new JorbisAudioFileReader().getAudioFileFormat(fSound);
            return true;
        } catch (UnsupportedAudioFileException | IOException e) {
            return false;
        }
    }

    public static boolean isFlac(File fSound) {
        try {
            return new FlacDecoder(fSound).isFlac();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static AudioFormat getPcmFormatByMpeg(AudioFormat baseFormat) {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                baseFormat.isBigEndian());
    }

    public static AudioInputStream decodeToPcm(
            AudioFormat baseFormat, AudioInputStream encodedAis) {
        AudioFormat decodedFormat = getPcmFormatByMpeg(baseFormat);
        // Es preferible realizar la comprobacion aca y en java interno
        // que tener una excepcion
        if (AudioSystem.isConversionSupported(decodedFormat, baseFormat))
            return AudioSystem.getAudioInputStream(decodedFormat, encodedAis);
        else
            return null;
    }

    public static AudioInputStream decodeToPcm(AudioInputStream sourceAis) {
        return AudioSystem.getAudioInputStream(AudioFormat
                .Encoding.PCM_SIGNED, sourceAis);
    }

}
