package cl.estencia.labs.muplayer.audio.io;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteOrder;

import static cl.estencia.labs.aucom.common.AudioConstants.MIDDLE_VOL;

public abstract class AudioIO {

    public static final float DEFAULT_VOLUME = MIDDLE_VOL;

    public boolean isSystemBigEndian() {
        return ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
    }

    public abstract AudioFormat convertToPcmFormat(AudioFormat baseFormat);

    public AudioInputStream getAudioSteamBySource(AudioFileReader audioReader, Object source) throws IOException, UnsupportedAudioFileException {
        if (source instanceof URL) {
            return audioReader.getAudioInputStream((URL) source);
        } else if (source instanceof InputStream) {
            return audioReader.getAudioInputStream((InputStream) source);
        } else {
            return audioReader.getAudioInputStream((File) source);
        }
    }

    public AudioFileFormat getAudioFileFormat(Object dataSource) throws UnsupportedAudioFileException, IOException {
        if (dataSource != null) {
            if (dataSource instanceof File) {
                return AudioSystem.getAudioFileFormat((File) dataSource);
            } else if (dataSource instanceof InputStream) {
                return AudioSystem.getAudioFileFormat((InputStream) dataSource);
            } else {
                return AudioSystem.getAudioFileFormat((URL) dataSource);
            }
        } else {
            return null;
        }
    }

    public AudioInputStream decodeToPcm(AudioFormat baseFormat, AudioInputStream encodedAis) {
        try {
            final AudioFormat decodedFormat = convertToPcmFormat(baseFormat);
            return AudioSystem.getAudioInputStream(decodedFormat, encodedAis);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public AudioInputStream decodeToPcm(AudioInputStream sourceAis) {
        return AudioSystem.getAudioInputStream(AudioFormat
                .Encoding.PCM_SIGNED, sourceAis);
    }
}
