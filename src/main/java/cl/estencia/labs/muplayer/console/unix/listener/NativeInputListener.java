package cl.estencia.labs.muplayer.console.unix.listener;

import cl.estencia.labs.muplayer.console.unix.event.NativeInputEvent;

public abstract class NativeInputListener<E extends NativeInputEvent> {
    public abstract void onInputEvent(E event);
}
