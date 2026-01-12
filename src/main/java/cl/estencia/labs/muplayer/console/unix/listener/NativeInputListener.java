package cl.estencia.labs.muplayer.console.unix.listener;

import cl.estencia.labs.muplayer.console.unix.event.NativeInputEvent;

public interface NativeInputListener<E extends NativeInputEvent> {
    void onInputEvent(E event);
}
