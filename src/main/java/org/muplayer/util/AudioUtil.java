package org.muplayer.util;

import org.muplayer.properties.AudioSupportInfo;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

public class AudioUtil {

    private static final float MAX_VOL = 0.855f;
    private static final float MIN_VOL = -80f;
    public static final float VOL_RANGE = MAX_VOL-MIN_VOL;
    public static final float MiDDLE_VOL = VOL_RANGE / 2;

    private static final float DEFAULT_MIN_VOL = 0;
    private static final float DEFAULT_MAX_VOL = 100;
    private static final float DEFAULT_VOL_RANGE = DEFAULT_MAX_VOL-DEFAULT_MIN_VOL;

    private static float convertVolRangeToLineRange(float volume, float minLineVol, float maxLineVol) {
        float volRange = maxLineVol-minLineVol;
        float volScale = 1 / (DEFAULT_VOL_RANGE / volRange);

        float result = (volume * volScale)+minLineVol;
        return result < minLineVol ? minLineVol : (Math.min(result, maxLineVol));
    }
    private static float convertLineRangeToVolRange(float volume, float minLineVol, float maxLineVol) {
        float volRange = maxLineVol-minLineVol;
        float volScale = 1 / (DEFAULT_VOL_RANGE / volRange);

        float result = (volume - minLineVol) / volScale;
        return result < DEFAULT_MIN_VOL ? DEFAULT_MIN_VOL : (Math.min(result, DEFAULT_MAX_VOL));
    }

    public static float convertVolRangeToLineRange(float volume) {
        return convertVolRangeToLineRange(volume, MIN_VOL, MAX_VOL);
    }

    public static float convertLineRangeToVolRange(float volume) {
        return convertLineRangeToVolRange(volume, MIN_VOL, MAX_VOL);
    }

    public static float convertVolRangeToLineRange(float volume, FloatControl control) {
        return convertVolRangeToLineRange(volume, control.getMinimum(), control.getMaximum());
    }

    public static float convertLineRangeToVolRange(float volume, FloatControl control) {
        return convertLineRangeToVolRange(volume, control.getMinimum(), control.getMaximum());
    }

    public static AudioInputStream instanceStream(AudioFileReader audioReader, Object source) throws IOException, UnsupportedAudioFileException {
        if (source instanceof URL)
            return audioReader.getAudioInputStream((URL) source);
        else if (source instanceof InputStream)
            return audioReader.getAudioInputStream((InputStream) source);
        else
            return audioReader.getAudioInputStream((File) source);
    }

    public static AudioFileFormat getAudioFileFormat(Object dataSource) throws UnsupportedAudioFileException, IOException {
        if (dataSource != null) {
            if (dataSource instanceof File)
                return AudioSystem.getAudioFileFormat((File) dataSource);
            else if (dataSource instanceof InputStream)
                return AudioSystem.getAudioFileFormat((InputStream) dataSource);
            else
                return AudioSystem.getAudioFileFormat((URL) dataSource);
        }
        return null;
    }

    public static boolean isSupported(File trackFile) {
        final String formatName = FileUtil.getFormatName(trackFile.getName());
        return AudioSupportInfo.getInstance().getProperty(formatName) != null;
    }

    public static boolean isSupported(Path track) {
        return isSupported(track.toFile());
    }

}
