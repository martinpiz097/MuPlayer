package org.muplayer.audio.player;

import lombok.Data;
import org.muplayer.audio.io.AudioIO;
import org.muplayer.audio.values.AudioConstantValues;
import org.muplayer.util.AudioConversionUtil;

import static org.muplayer.audio.values.AudioConstantValues.MiDDLE_VOL;

@Data
public class PlayerData {
    private volatile int trackIndex;
    private volatile float volume;
    private volatile boolean on;
    private volatile boolean isMute;

    public static final float DEFAULT_VOLUME = AudioConversionUtil.convertLineRangeToVolRange(MiDDLE_VOL);

    public PlayerData() {
        this.trackIndex = -1;
        this.volume = DEFAULT_VOLUME;
        this.on = false;
        this.isMute = false;
    }

    public synchronized boolean isVolumeZero() {
        return volume == 0;
    }

    public synchronized void setVolume(float volume) {
        this.volume = volume > 100 ? 100 : (volume < 0 ? 0 : volume);
        isMute = isVolumeZero();
    }

    public synchronized void increaseTrackIndex(int steps) {
        trackIndex+=steps;
    }

    public synchronized void decreaseTrackIndex(int steps) {
        trackIndex-=steps;
    }
}
