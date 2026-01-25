package cl.estencia.labs.muplayer.core.aucom.event.input;

import static cl.estencia.labs.muplayer.core.aucom.common.IOConstants.DEFAULT_BUFF_SIZE;

public interface MicrophoneEvent {
    default void onAudioRead(byte[] buffer) {
        onAudioRead(buffer, DEFAULT_BUFF_SIZE);
    }
    void onAudioRead(byte[] buffer, int length);
}
