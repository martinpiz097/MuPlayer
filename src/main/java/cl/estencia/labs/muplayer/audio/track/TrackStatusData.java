package cl.estencia.labs.muplayer.audio.track;

import lombok.*;

import static cl.estencia.labs.aucom.core.util.AudioDecodingUtil.DEFAULT_VOLUME;

@Data
@AllArgsConstructor
@Builder
public class TrackStatusData {
    private volatile double secsSeeked;
    private volatile double bytesPerSecond;
    private volatile float volume;
    private volatile boolean isMute;

    public TrackStatusData() {
        this.secsSeeked = 0;
        this.bytesPerSecond = 0;
        this.volume = DEFAULT_VOLUME;
        this.isMute = false;
        this.canTrackContinue = true;
    }

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
