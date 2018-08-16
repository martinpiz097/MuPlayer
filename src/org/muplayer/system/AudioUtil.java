package org.muplayer.system;

public class AudioUtil {
    public static float convertVolRangeToLineRange(float volume) {
        return (float) ((0.855*volume)-80.0);
    }
    public static float convertLineRangeToVolRange(float volume) {
        return (float) ((volume+80.0) / 0.855);
    }

}
