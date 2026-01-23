package cl.estencia.labs.muplayer.core.aucom.device.output;

import cl.estencia.labs.muplayer.core.aucom.device.DataAudioDevice;
import cl.estencia.labs.muplayer.core.aucom.util.VolumeConverter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;

import static cl.estencia.labs.muplayer.core.aucom.common.IOConstants.EOF;

public abstract class AudioOutputDevice<D extends DataLine> extends DataAudioDevice<D, DataLine.Info> {

    protected final VolumeConverter volumeConverter;

    public AudioOutputDevice() {
        super();
        this.volumeConverter = new VolumeConverter();
    }

    public AudioOutputDevice(AudioFormat quality) {
        super(quality);
        this.volumeConverter = new VolumeConverter();
    }

    public AudioOutputDevice(D driver) {
        super(driver);
        this.volumeConverter = new VolumeConverter();
    }

    public float getVolume() {
        FloatControl floatControl = getControl(FloatControl.Type.MASTER_GAIN);
        return floatControl != null
                ? volumeConverter.dataLineVolumeToVolume(floatControl.getValue())
                : EOF;
    }

    public boolean setVolume(float volume){
        FloatControl floatControl = getControl(FloatControl.Type.MASTER_GAIN);
        if (floatControl != null) {
            floatControl.setValue(volumeConverter.volumeToDataLineVolume(volume));
            return true;
        } else {
            return false;
        }
    }

}
