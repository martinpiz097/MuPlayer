package org.muplayer.util;

import org.muplayer.info.AudioHardware;

import javax.sound.sampled.FloatControl;

public class LineUtil {
    public static float getFormattedMasterVolume() {
        final FloatControl volumeControl = AudioHardware.getReadyVolumeControl();
        return AudioUtil.convertLineRangeToVolRange(volumeControl.getValue(), volumeControl);
    }

    public static void setFormattedMasterVolume(float volume) {
        final FloatControl volumeControl = AudioHardware.getReadyVolumeControl();
        volumeControl.setValue(AudioUtil.convertVolRangeToLineRange(volume, volumeControl));
    }
}
