package cl.estencia.labs.muplayer.core.aucom.device.input;

import cl.estencia.labs.muplayer.core.aucom.device.input.AudioInputDevice;
import lombok.extern.java.Log;
import cl.estencia.labs.aucom.event.input.listener.MicrophoneListener;
import cl.estencia.labs.aucom.event.input.MicrophoneEvent;

import javax.sound.sampled.*;

/**
 * @author martin
 */
@Slf4j
public class Microphone extends AudioInputDevice {
    private volatile MicrophoneListener listener;

    public Microphone() {
        super();
        this.listener = createListener();
    }

    public Microphone(AudioFormat quality) {
        super(quality);
        this.listener = createListener();
    }

    public Microphone(TargetDataLine driver) {
        super(driver);
        this.listener = createListener();
    }

    @Override
    protected synchronized DataLine.Info getLineInfo(AudioFormat format) {
        return new DataLine.Info(TargetDataLine.class, format);
    }

    private boolean isListenerAlive() {
        return listener != null && listener.isAlive();
    }

    private boolean startListener() {
        if (listener == null) {
            listener = createListener();
        }
        if (!isListenerAlive()) {
            listener.start();
            return true;
        } else {
            return false;
        }
    }

    private boolean closeListener() {
        if (listener == null) {
            return true;
        }
        if (isListenerAlive()) {
            listener.interrupt();
            listener = null;
            return true;
        } else {
            return false;
        }
    }

    private MicrophoneListener createListener() {
        return new MicrophoneListener(this);
    }

    public void listen(MicrophoneEvent event) {
        listener.addEvent(event);
    }

    @Override
    public synchronized boolean open() {
        return super.open() && startListener();
    }

    @Override
    public synchronized boolean close() {
        return closeListener() && super.close();
    }

}
