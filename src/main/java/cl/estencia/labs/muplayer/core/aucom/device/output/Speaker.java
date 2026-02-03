package cl.estencia.labs.muplayer.core.aucom.device.output;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

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
