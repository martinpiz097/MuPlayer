package org.muplayer.audio.player;

import lombok.Data;
import lombok.Synchronized;
import org.muplayer.util.AudioUtil;

@Data
public class PlayerData {
    private volatile int trackIndex;
    private volatile float currentVolume;
    private volatile boolean on;
    private volatile boolean isMute;

    public static final float DEFAULT_VOLUME = AudioUtil.convertLineRangeToVolRange(AudioUtil.MiDDLE_VOL);

    public PlayerData() {
        trackIndex = -1;
        currentVolume = DEFAULT_VOLUME;
        on = false;
        isMute = false;
    }

    public synchronized void increaseTrackIndex(int steps) {
        trackIndex+=steps;
    }

    public synchronized void decreaseTrackIndex(int steps) {
        trackIndex-=steps;
    }
}
