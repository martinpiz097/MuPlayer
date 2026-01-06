package cl.estencia.labs.muplayer.console.unix.listener;

import cl.estencia.labs.muplayer.console.unix.event.LineInputEvent;

public abstract class LineInputListener extends NativeInputListener<LineInputEvent> {

    public abstract void onInput(LineInputEvent event);
}
