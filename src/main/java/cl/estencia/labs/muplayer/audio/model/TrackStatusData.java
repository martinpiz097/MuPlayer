package cl.estencia.labs.muplayer.audio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TrackStatusData extends VolumeStatusData {
    private volatile double secsSeeked;
    private volatile double bytesPerSecond;

    public TrackStatusData() {
        super();
        this.secsSeeked = 0;
        this.bytesPerSecond = 0;
        this.canTrackContinue = true;
    }

    private volatile boolean canTrackContinue;

    public boolean canTrackContinue() {
        return canTrackContinue;
    }

}
