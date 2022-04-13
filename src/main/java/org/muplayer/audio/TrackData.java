package org.muplayer.audio;

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

    public float getGain() {
        return isMute ? 0 : volume;
    }
}
