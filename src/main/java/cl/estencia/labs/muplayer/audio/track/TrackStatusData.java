package cl.estencia.labs.muplayer.audio.track;

import cl.estencia.labs.aucom.common.AudioConstants;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackStatusData {
    private volatile double secsSeeked;
    private volatile double bytesPerSecond;
    private volatile float volume;
    private volatile boolean isMute;

    @Getter(AccessLevel.NONE)
    private volatile boolean canTrackContinue;

    public synchronized boolean isVolumeZero() {
        return volume == 0;
    }

    public boolean canTrackContinue() {
        return canTrackContinue;
    }

    public synchronized float getVolume() {
        return isMute ? 0 : volume;
    }

    // muchas llamadas a setVolume en un comienzo?
    public synchronized void setVolume(float volume) {
        this.volume = volume > 100 ? 100 : (volume < 0 ? 0 : volume);
        isMute = isVolumeZero();
    }
}
