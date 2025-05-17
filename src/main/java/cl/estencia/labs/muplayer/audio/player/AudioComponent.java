package cl.estencia.labs.muplayer.audio.player;

import cl.estencia.labs.aucom.core.util.AudioSystemManager;

public abstract class AudioComponent extends Thread {
    protected final AudioSystemManager audioSystemManager;

    protected AudioComponent() {
        this.audioSystemManager = new AudioSystemManager();
    }

    public float getSystemVolume() {
        return audioSystemManager.getFormattedSpeakerVolume();
    }
    public void setSystemVolume(float volume) {
        audioSystemManager.setFormattedSpeakerVolume(volume);
    }

}
