package cl.estencia.labs.muplayer.core.aucom.device;

import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;

import static cl.estencia.labs.muplayer.core.aucom.common.AudioQuality.DEFAULT_QUALITY;

@Slf4j
public abstract class DataAudioDevice<D extends DataLine, I extends DataLine.Info>
        extends AudioDevice<D, I> {

    protected volatile AudioFormat audioFormat;

    public DataAudioDevice() {
        super();
    }

    public DataAudioDevice(AudioFormat quality) {
        this();
        this.audioFormat = quality;
    }

    public DataAudioDevice(D driver) {
        super(driver);
        this.audioFormat = driver.getFormat();
    }

    @Override
    protected boolean setupDriver(AudioFormat audioFormat) {
        if (audioFormat != null) {
            setupDriver(initAudioDevice(audioFormat));
            return setupDriver(driver);
        }
        return false;
    }

    @Override
    protected boolean setupDriver(D driver) {
        if (!close()) {
            return false;
        }

        this.driver = driver;
        this.audioFormat = driver.getFormat();
        return true;
    }

    protected AudioFormat getDefaultFormat() {
        return DEFAULT_QUALITY;
    }

    public boolean isRunning() {
        return super.isOpen() && driver.isRunning();
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public void setAudioFormat(AudioFormat format) {
        setupDriver(format);
    }

    @Override
    public synchronized boolean open() {
        AudioFormat openFormat = audioFormat != null ? audioFormat : getDefaultFormat();
        return open(openFormat);
    }

    public boolean open(AudioFormat audioFormat) {
        if (isOpen() || audioFormat == null) {
            return false;
        }

        driver = initAudioDevice(audioFormat);
        if (driver != null) {
            try {
                driver.open();
                driver.start();
                return true;
            } catch (LineUnavailableException e) {
                log.error("Error on open driver ("
                        +e.getClass().getSimpleName()
                        +"): " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    public boolean reopen(AudioFormat audioFormat) {
        boolean closed;
        if (isOpen()) {
            closed = close();
            if (!closed) {
                return false;
            }
        }
        return open(audioFormat);
    }
}
