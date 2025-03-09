package org.muplayer.audio.player;

import org.muplayer.audio.info.AudioHardware;
import org.muplayer.util.AudioUtil;

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
