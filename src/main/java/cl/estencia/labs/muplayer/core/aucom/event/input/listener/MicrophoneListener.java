package cl.estencia.labs.muplayer.core.aucom.event.input.listener;

import cl.estencia.labs.muplayer.core.aucom.device.input.AudioInputDevice;
import cl.estencia.labs.muplayer.core.aucom.device.input.Microphone;
import cl.estencia.labs.muplayer.core.aucom.event.input.MicrophoneEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MicrophoneListener extends Thread {
    private final AudioInputDevice microphone;
    private final List<MicrophoneEvent> listEvents;

    public MicrophoneListener(Microphone microphone) {
        this.microphone = microphone;
        this.listEvents = new ArrayList<>();
    }

    public void addEvent(MicrophoneEvent microphoneEvent) {
        listEvents.add(microphoneEvent);
    }

    @Override
    public void run() {
        try {
            log.info("MicrophoneListener started!");
            while (microphone.isOpen()) {
                while (listEvents.isEmpty()) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                listEvents.forEach(event ->
                        event.onAudioRead(microphone.readAudio()));
            }
        } catch (Exception e) {

        }

        log.info("MicrophoneListener finished!");
    }
}
