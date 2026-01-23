package cl.estencia.labs.muplayer.core.aucom.device.input;

import cl.estencia.labs.muplayer.core.aucom.device.DataAudioDevice;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import static cl.estencia.labs.muplayer.core.aucom.common.IOConstants.DEFAULT_BUFF_SIZE;

public abstract class AudioInputDevice extends DataAudioDevice<TargetDataLine, DataLine.Info> {

    public AudioInputDevice() {
        super();
    }

    public AudioInputDevice(AudioFormat quality) {
        super(quality);
    }

    public AudioInputDevice(TargetDataLine driver) {
        super(driver);
    }

    public AudioInputStream getAudioInputStream() {
        return new AudioInputStream(driver);
    }

    public byte[] readAudio() {
        return readAudio(DEFAULT_BUFF_SIZE);
    }

    public byte[] readAudio(int len) {
        byte[] audioBuff = new byte[len];
        driver.read(audioBuff, 0, len);
        return audioBuff;
    }

    public int readAudio(byte[] buffer, int off, int len) {
        if (off >= len)
            throw new IndexOutOfBoundsException();
        if (off < 0)
            off = 0;
        if (len > buffer.length)
            len = buffer.length;
        return driver.read(buffer, off, len);
    }
}
