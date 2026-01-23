package cl.estencia.labs.muplayer.core.aucom.device;

import cl.estencia.labs.muplayer.core.aucom.device.VirtualOutput;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.*;

/**
 *
 * @author martin
 */
@Slf4j
public abstract class AudioDevice<D extends Line, I extends Line.Info> {
    protected volatile D driver;

    public AudioDevice() {
        this(null);
    }

    public AudioDevice(D driver) {
        this.driver = driver;
    }

    protected abstract boolean setupDriver(AudioFormat audioFormat);
    protected abstract boolean setupDriver(D driver);

    protected D initAudioDevice(AudioFormat audioFormat) {
        try {
            return (D) VirtualOutput.getVirtualLine(audioFormat);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
//        return initAudioDevice(getLineInfo(audioFormat));
    }

    protected D initAudioDevice(I driverInfo) {
        try {
            return (D) AudioSystem.getLine(driverInfo);
        } catch (LineUnavailableException e) {
            log.error("Error on audioDevice initialization "
                    + "("+e.getClass().getSimpleName()+"): "
                    + e.getMessage());
            return null;
        }
    }

    protected abstract I getLineInfo(AudioFormat format);

    public boolean isOpen() {
        return driver != null && driver.isOpen();
    }

    public D getDriver() {
        return driver;
    }

    public void setDriver(D driver) {
        this.driver = driver;
    }

    public I getDriverInfo() {
        return driver != null ? (I) driver.getLineInfo() : null;
    }

    public void setDriverInfo(I info) {
        close();
        driver = initAudioDevice(info);
    }

    public synchronized <C extends Control> C getControl(Control.Type type) {
        try {
            return (C) driver.getControl(type);
        } catch (IllegalArgumentException e) {
            log.error("Error on getControl ("
                    +e.getClass().getSimpleName()
                    +"): " + e.getMessage());
            return null;
        }
    }

    public abstract boolean open();

    public synchronized boolean close() {
        if (isOpen()) {
            driver.close();
            return true;
        }
        return false;
    }

    public boolean reopen() {
        if (isOpen() && !close()) {
            return false;
        }

        return open();
    }

}
