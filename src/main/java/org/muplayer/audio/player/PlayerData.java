package org.muplayer.audio.player;

import lombok.Data;
import org.muplayer.util.AudioUtil;

import static org.muplayer.audio.values.AudioConstantValues.MIDDLE_VOL;
import static org.muplayer.util.AudioUtil.DEFAULT_VOLUME;

@Data
public class PlayerData {
    private volatile int trackIndex;
    private volatile float volume;
    private volatile boolean on;
    private volatile boolean isMute;

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
