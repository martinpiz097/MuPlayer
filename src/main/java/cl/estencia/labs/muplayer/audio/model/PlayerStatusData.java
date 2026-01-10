package cl.estencia.labs.muplayer.audio.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerStatusData extends VolumeStatusData {
    private volatile int currentTrackIndex;
    private volatile boolean on;

    public PlayerStatusData() {
        super();
        this.currentTrackIndex = -1;
        this.on = false;
    }

    public synchronized void increaseTrackIndex(int steps) {
        currentTrackIndex += steps;
    }

    public synchronized void decreaseTrackIndex(int steps) {
        currentTrackIndex -= steps;
    }

}
