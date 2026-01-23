package cl.estencia.labs.muplayer.core.aucom.device;

import javax.sound.sampled.*;

public class VirtualOutput {

    public static Mixer findVirtualMixer() {
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            if (info.getName().toLowerCase().contains("virtual") ||
                    info.getDescription().toLowerCase().contains("virtual")) {
                return AudioSystem.getMixer(info);
            }
        }

        return null;
    }

    public static SourceDataLine getVirtualLine(AudioFormat format) throws LineUnavailableException {
        Mixer mixer = findVirtualMixer();
        if (mixer != null) {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            return (SourceDataLine) mixer.getLine(info);
        }
        throw new LineUnavailableException("Virtual mixer not found");
    }

}