package org.muplayer.audio.io;

import org.muplayer.audio.track.io.AudioDataInputStream;
import org.muplayer.data.properties.support.AudioSupportInfo;
import org.muplayer.util.FileUtil;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.file.Path;

public abstract class AudioIO {
    public static boolean isSupportedFile(File trackFile) {
        final String formatName = FileUtil.getFormatName(trackFile.getName());
        return AudioSupportInfo.getInstance().getProperty(formatName) != null;
    }

    public static boolean isSupportedFile(Path track) {
        return isSupportedFile(track.toFile());
    }

    public static boolean isSupportedFile(String trackPath) {
        return isSupportedFile(new File(trackPath));
    }

    public boolean isSystemBigEndian() {
        return ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
    }

    public abstract AudioFormat getPcmFormat(AudioFormat baseFormat);

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
            final AudioFormat decodedFormat = getPcmFormat(baseFormat);
            return AudioSystem.getAudioInputStream(decodedFormat, encodedAis);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public AudioDataInputStream decodeToPcmNew(
            AudioFormat baseFormat, AudioInputStream encodedAis) {
        AudioInputStream audioInputStream = decodeToPcm(baseFormat, encodedAis);
        //AudioSystem.getAudioInputStream();

        return null;
    }

    public AudioInputStream decodeToPcm(AudioInputStream sourceAis) {
        return AudioSystem.getAudioInputStream(AudioFormat
                .Encoding.PCM_SIGNED, sourceAis);
    }
}
