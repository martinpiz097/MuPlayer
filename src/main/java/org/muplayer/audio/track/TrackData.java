package org.muplayer.audio.track;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackData {
    private volatile double secsSeeked;
    private volatile double bytesPerSecond;
    private volatile float volume;
    private volatile boolean isMute;

    public synchronized boolean isVolumeZero() {
        return volume == 0;
    }

    public synchronized float getVolume() {
        return isMute ? 0 : volume;
    }

    public synchronized void setVolume(float volume) {
        this.volume = volume > 100 ? 100 : (volume < 0 ? 0 : volume);
        isMute = isVolumeZero();
    }
}
