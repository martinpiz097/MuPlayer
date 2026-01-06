package cl.estencia.labs.muplayer.console.unix.listener;

import cl.estencia.labs.muplayer.console.unix.event.KeyInputEvent;

public abstract class KeyInputListener extends NativeInputListener<KeyInputEvent> {
    protected final char key;

    protected KeyInputListener(char key) {
        this.key = key;
    }

    protected KeyInputListener(int key) {
        this.key = (char) key;
    }

    public boolean isKey(int key) {
        return this.key == key;
    }

    public abstract void onInput(KeyInputEvent event);
}
