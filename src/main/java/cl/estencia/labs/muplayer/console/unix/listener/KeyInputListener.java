package cl.estencia.labs.muplayer.console.unix.listener;

import cl.estencia.labs.muplayer.console.unix.event.KeyInputEvent;

public abstract class KeyInputListener extends NativeInputListener<KeyInputEvent> {
    protected final Character key;

    public KeyInputListener() {
        this((Character) null);
    }

    protected KeyInputListener(Character key) {
        this.key = key;
    }

    protected KeyInputListener(Integer key) {
        this.key = key != null ? (char) key.intValue() : null;
    }

    // modo generico
    public boolean isKey(int key) {
        return this.key == null || this.key == key;
    }

    public abstract void onInput(KeyInputEvent event);
}
