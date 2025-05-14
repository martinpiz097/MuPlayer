package cl.estencia.labs.muplayer.audio.player;

import cl.estencia.labs.aucom.audio.AudioHardware;

public abstract class AudioComponent extends Thread {
    protected final AudioHardware audioHardware;

    protected AudioComponent() {
        this.audioHardware = new AudioHardware();
    }

    public float getSystemVolume() {
        return new AudioHardware().getFormattedSpeakerVolume();
    }
    public void setSystemVolume(float volume) {
        audioHardware.setFormattedSpeakerVolume(volume);
    }

}
