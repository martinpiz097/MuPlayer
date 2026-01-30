package cl.estencia.labs.muplayer.console.unixconsole.listener;

import cl.estencia.labs.muplayer.console.unixconsole.event.NativeInputEvent;

public interface NativeInputListener<E extends NativeInputEvent> {
    void onInputEvent(E event);
}
