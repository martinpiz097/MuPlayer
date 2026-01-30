package cl.estencia.labs.muplayer.core.aucom.util;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static cl.estencia.labs.muplayer.core.aucom.common.AudioConstants.DEFAULT_MAX_VOL;

public class AudioDecodingUtil {

    public static final float DEFAULT_VOLUME = DEFAULT_MAX_VOL;

    public AudioInputStream getSourceAudioStream(Object source) throws IOException, UnsupportedAudioFileException {
        if (source instanceof File) {
            return AudioSystem.getAudioInputStream((File) source);
        } else if (source instanceof InputStream) {
            return AudioSystem.getAudioInputStream((InputStream) source);
        } else {
            return AudioSystem.getAudioInputStream((URL) source);
        }
    }

    public AudioInputStream getSourceAudioStream(AudioFileReader audioReader, Object source) throws IOException, UnsupportedAudioFileException {
        if (source == null) {
            return null;
        }
        if (source instanceof File) {
            return audioReader.getAudioInputStream((File) source);
        } else if (source instanceof InputStream) {
            return audioReader.getAudioInputStream((InputStream) source);
        } else {
            return audioReader.getAudioInputStream((URL) source);
        }
    }

    public AudioFileFormat getAudioFileFormat(Object source) throws IOException, UnsupportedAudioFileException {
        if (source == null) {
            return null;
        }
        if (source instanceof File) {
            return AudioSystem.getAudioFileFormat((File) source);
        } else if (source instanceof InputStream) {
            return AudioSystem.getAudioFileFormat((InputStream) source);
        } else {
            return AudioSystem.getAudioFileFormat((URL) source);
        }
    }

    public AudioInputStream decodeToPcm(AudioInputStream sourceAis, AudioFormat pcmFormat) {
        return AudioSystem.getAudioInputStream(pcmFormat, sourceAis);
    }

}
