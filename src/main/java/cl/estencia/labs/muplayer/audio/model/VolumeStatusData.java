package cl.estencia.labs.muplayer.audio.model;

import lombok.Getter;
import lombok.Setter;

import static cl.estencia.labs.aucom.core.util.AudioDecodingUtil.DEFAULT_VOLUME;

@Getter
@Setter
public abstract class VolumeStatusData {
    protected volatile float volume;
    protected volatile boolean isMute;

    protected VolumeStatusData() {
        this.volume = DEFAULT_VOLUME;
        this.isMute = false;
    }

    public synchronized boolean isVolumeZero() {
        return volume == 0;
    }

    public synchronized float getVolume() {
        return volume;
    }

    public synchronized void setVolume(float volume) {
        this.volume = volume > 100 ? 100 : (volume < 0 ? 0 : volume);
        this.isMute = isVolumeZero();
    }

}
