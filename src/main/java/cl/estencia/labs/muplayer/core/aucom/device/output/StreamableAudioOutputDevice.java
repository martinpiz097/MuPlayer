package cl.estencia.labs.muplayer.core.aucom.device.output;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import java.nio.ByteBuffer;

import static cl.estencia.labs.muplayer.core.aucom.common.IOConstants.OFFSET;

public abstract class StreamableAudioOutputDevice extends AudioOutputDevice<SourceDataLine> {

    public StreamableAudioOutputDevice() {
        super();
    }

    public StreamableAudioOutputDevice(AudioFormat quality) {
        super(quality);
    }

    public StreamableAudioOutputDevice(SourceDataLine driver) {
        super(driver);
    }

    public void playAudio(byte[] audioBuff){
        if(audioBuff == null) {
            return;
        }

        playAudio(audioBuff, audioBuff.length);
    }

    public void playAudio(byte[] audioBuff, int len){
        driver.write(audioBuff, OFFSET, Math.min(len, audioBuff.length));
    }

    public void playAudio(ByteBuffer audioBuffer) {
        if (audioBuffer == null)
            return;

        playAudio(audioBuffer.array(), audioBuffer.capacity());
    }

    public void playAudio(ByteBuffer audioBuffer, int len) {
        if (audioBuffer == null)
            return;

        playAudio(audioBuffer.array(), len);
    }

}
