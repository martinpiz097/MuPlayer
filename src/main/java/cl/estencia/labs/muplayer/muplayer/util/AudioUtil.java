package cl.estencia.labs.muplayer.muplayer.util;

import cl.estencia.labs.muplayer.muplayer.data.properties.support.AudioSupportInfo;

import javax.sound.sampled.FloatControl;

import java.io.File;
import java.nio.file.Path;

import static cl.estencia.labs.muplayer.muplayer.audio.values.AudioConstantValues.*;

public class AudioUtil {
    private final AudioSupportInfo audioSupportInfo;

    public static final float DEFAULT_VOLUME;

    static {
        DEFAULT_VOLUME = new AudioUtil().convertLineRangeToVolRange(MIDDLE_VOL);
    }

    public AudioUtil() {
        this.audioSupportInfo = new AudioSupportInfo();
    }

    public boolean isSupportedFile(File trackFile) {
        final String formatName = FileUtil.getFormatName(trackFile.getName());
        return audioSupportInfo.getProperty(formatName) != null;
    }

    public boolean isSupportedFile(Path track) {
        return isSupportedFile(track.toFile());
    }

    public boolean isSupportedFile(String trackPath) {
        return isSupportedFile(new File(trackPath));
    }

    public float convertVolRangeToLineRange(float volume, float minLineVol, float maxLineVol) {
        float volRange = maxLineVol - minLineVol;
        float volScale = 1 / (DEFAULT_VOL_RANGE / volRange);
        float result = (volume * volScale) + minLineVol;

        return result < minLineVol ? minLineVol : (Math.min(result, maxLineVol));
    }

    public float convertLineRangeToVolRange(float volume, float minLineVol, float maxLineVol) {
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
}
