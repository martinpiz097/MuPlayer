package cl.estencia.labs.muplayer.console.unix.listener;

import cl.estencia.labs.muplayer.console.unix.event.KeyCombinationEvent;

import static cl.estencia.labs.muplayer.core.util.IOUtil.createCharArrayFromInts;

public abstract class KeyCombinationListener implements NativeInputListener<KeyCombinationEvent> {
    protected final char[] keys;

    public KeyCombinationListener(char... keys) {
        if (keys == null || keys.length == 0) {
            throw new NullPointerException("Key combination parameter is null!");
        }

        this.keys = keys;
    }

    public KeyCombinationListener(int... keys) {
        if (keys == null || keys.length == 0) {
            throw new NullPointerException("Key combination parameter is null!");
        }

        this.keys = createCharArrayFromInts(keys);
    }

    public boolean isKeysCombination(int... keys) {
        if (keys == null || keys.length == 0) {
            return false;
        }

        if (this.keys.length != keys.length) {
            return false;
        }

        for (int i = 0; i < keys.length; i++) {
            if (this.keys[i] != keys[i]) {
                return false;
            }
        }

        return true;
    }

    protected abstract void onCombination(KeyCombinationEvent event);

    @Override
    public void onInputEvent(KeyCombinationEvent event) {
        if (!isKeysCombination(event.getKeys())) {
            return;
        }

        onCombination(event);
    }

}
