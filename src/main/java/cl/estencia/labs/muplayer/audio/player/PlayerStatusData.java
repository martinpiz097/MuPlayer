package cl.estencia.labs.muplayer.audio.player;

import lombok.Data;

import static cl.estencia.labs.muplayer.audio.io.AudioIO.DEFAULT_VOLUME;

@Data
public class PlayerStatusData {
    private volatile int trackIndex;
    private volatile float volume;
    private volatile boolean on;
    private volatile boolean isMute;

    public PlayerStatusData() {
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
