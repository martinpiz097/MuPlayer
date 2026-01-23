package cl.estencia.labs.muplayer.core.aucom.device.output;

import cl.estencia.labs.muplayer.core.aucom.device.output.StreamableAudioOutputDevice;

import javax.sound.sampled.*;

/**
 *
 * @author martin
 */
public class Speaker extends StreamableAudioOutputDevice {
    public Speaker() {
        super();
    }

    public Speaker(AudioFormat quality) {
        super(quality);
    }

    public Speaker(SourceDataLine driver) {
        super(driver);
    }

    public Speaker(AudioInputStream audioInputStream) {
        this(audioInputStream != null ? audioInputStream.getFormat() : null);
    }

    @Override
    protected synchronized DataLine.Info getLineInfo(AudioFormat format) {
        return new DataLine.Info(SourceDataLine.class, format);
    }

}
