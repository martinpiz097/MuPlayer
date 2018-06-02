package org.muplayer.audio;

public class AudioUtil {
    public static float convertVolRangeToLineRange(float volume) {
        return (float) (-80.0+(0.855*volume));
    }

    public static float convertLineRangeToVolRange(float volume) {
        return (float) ((volume+80.0) / 0.855);
    }

}
