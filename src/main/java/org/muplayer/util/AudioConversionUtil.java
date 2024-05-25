package org.muplayer.util;

import javax.sound.sampled.FloatControl;

import static org.muplayer.audio.values.AudioConstantValues.*;

public class AudioConversionUtil {
    public static float convertVolRangeToLineRange(float volume, float minLineVol, float maxLineVol) {
        float volRange = maxLineVol - minLineVol;
        float volScale = 1 / (DEFAULT_VOL_RANGE / volRange);
        float result = (volume * volScale) + minLineVol;

        return result < minLineVol ? minLineVol : (Math.min(result, maxLineVol));
    }

    public static float convertLineRangeToVolRange(float volume, float minLineVol, float maxLineVol) {
        float volRange = maxLineVol - minLineVol;
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
}
