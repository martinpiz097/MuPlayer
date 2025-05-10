package cl.estencia.labs.muplayer.audio.player;

import cl.estencia.labs.aucom.audio.AudioHardware;
import cl.estencia.labs.muplayer.util.AudioSupportUtil;

public abstract class AudioComponent extends Thread {
    protected final AudioHardware audioHardware;
    protected final AudioSupportUtil audioSupportUtil;

    protected AudioComponent() {
        this.audioHardware = new AudioHardware();
        this.audioSupportUtil = new AudioSupportUtil();
    }

    public float getSystemVolume() {
        return new AudioHardware().getFormattedSpeakerVolume();
    }
    public void setSystemVolume(float volume) {
        audioHardware.setFormattedSpeakerVolume(volume);
    }

}
