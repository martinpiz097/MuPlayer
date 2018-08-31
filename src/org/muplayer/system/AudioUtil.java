package org.muplayer.system;

import java.io.File;

import static org.muplayer.audio.util.AudioExtensions.SUPPORTEDEXTENSIONS;

public class AudioUtil {

    private static final float DELIMITER = 0.855f;
    public static float convertVolRangeToLineRange(float volume) {
        return (float) ((DELIMITER *volume)-80.0);
    }
    public static float convertLineRangeToVolRange(float volume) {
        return (float) ((volume+80.0) / DELIMITER);
    }

    public static boolean isSupported(File track) {
        String trackName = track.getName();
        boolean isSupported = false;

        for (int i = 0; i < SUPPORTEDEXTENSIONS.length; i++) {
            if (trackName.endsWith(SUPPORTEDEXTENSIONS[i])) {
                isSupported = true;
                break;
            }
        }
        return isSupported;
    }

}
