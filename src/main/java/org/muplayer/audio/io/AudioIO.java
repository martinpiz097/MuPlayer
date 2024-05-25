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
import java.nio.file.Path;

public abstract class AudioIO {
    protected static final float MAX_VOL = 0.855f;
    protected static final float MIN_VOL = -80f;
    public static final float VOL_RANGE = MAX_VOL - MIN_VOL;
    public static final float MiDDLE_VOL = VOL_RANGE / 2;

    protected static final float DEFAULT_MIN_VOL = 0;
    protected static final float DEFAULT_MAX_VOL = 100;
    protected static final float DEFAULT_VOL_RANGE = DEFAULT_MAX_VOL - DEFAULT_MIN_VOL;

    protected float convertVolRangeToLineRange(float volume, float minLineVol, float maxLineVol) {
        float volRange = maxLineVol - minLineVol;
        float volScale = 1 / (DEFAULT_VOL_RANGE / volRange);

        float result = (volume * volScale) + minLineVol;
        return result < minLineVol ? minLineVol : (Math.min(result, maxLineVol));
    }

    protected float convertLineRangeToVolRange(float volume, float minLineVol, float maxLineVol) {
        float volRange = maxLineVol - minLineVol;
        float volScale = 1 / (DEFAULT_VOL_RANGE / volRange);

        float result = (volume - minLineVol) / volScale;
        return result < DEFAULT_MIN_VOL ? DEFAULT_MIN_VOL : (Math.min(result, DEFAULT_MAX_VOL));
    }

    public float convertVolRangeToLineRange(float volume) {
        return convertVolRangeToLineRange(volume, MIN_VOL, MAX_VOL);
    }

    public float convertLineRangeToVolRange(float volume) {
        return convertLineRangeToVolRange(volume, MIN_VOL, MAX_VOL);
    }

    public float convertVolRangeToLineRange(float volume, FloatControl control) {
        return convertVolRangeToLineRange(volume, control.getMinimum(), control.getMaximum());
    }

    public float convertLineRangeToVolRange(float volume, FloatControl control) {
        return convertLineRangeToVolRange(volume, control.getMinimum(), control.getMaximum());
    }

    public boolean isSupportedFile(File trackFile) {
        final String formatName = FileUtil.getFormatName(trackFile.getName());
        return AudioSupportInfo.getInstance().getProperty(formatName) != null;
    }

    public boolean isSupportedFile(Path track) {
        return isSupportedFile(track.toFile());
    }

    public boolean isSupportedFile(String trackPath) {
        return isSupportedFile(new File(trackPath));
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
