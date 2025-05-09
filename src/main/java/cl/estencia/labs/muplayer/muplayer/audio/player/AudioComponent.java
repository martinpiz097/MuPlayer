package cl.estencia.labs.muplayer.muplayer.audio.player;

import cl.estencia.labs.muplayer.muplayer.audio.info.AudioHardware;
import cl.estencia.labs.muplayer.muplayer.util.AudioUtil;

public abstract class AudioComponent extends Thread {
    protected final AudioHardware audioHardware;
    protected final AudioUtil audioUtil;

    protected AudioComponent() {
        this.audioHardware = new AudioHardware();
        this.audioUtil = new AudioUtil();
    }

    public float getSystemVolume() {
        return new AudioHardware().getFormattedSpeakerVolume();
    }
    public void setSystemVolume(float volume) {
        audioHardware.setFormattedSpeakerVolume(volume);
    }

}
