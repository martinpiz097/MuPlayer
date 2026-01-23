package cl.estencia.labs.muplayer.console.unix.listener;

import cl.estencia.labs.muplayer.console.unix.event.AltKeyCombinationEvent;
import cl.estencia.labs.muplayer.core.exception.MuPlayerException;
import cl.estencia.labs.muplayer.core.exception.MuPlayerRuntimeException;

import java.util.Arrays;

import static cl.estencia.labs.muplayer.core.util.IOUtil.createCharArrayFromInts;

public abstract class AltKeyCombinationListener implements NativeInputListener<AltKeyCombinationEvent> {
    protected final char[] keys;

    public AltKeyCombinationListener(int... keys) {
        this(createCharArrayFromInts(keys));
    }

    public AltKeyCombinationListener(char... keys) {
        if (keys == null || keys.length == 0) {
            throw new NullPointerException("Key combination parameter is null!");
        }
        if (keys.length != 2) {
            throw new MuPlayerRuntimeException("Not valid ALT combination: " + Arrays.toString(keys));
        }

        this.keys = keys;
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

    protected abstract void onCombination(AltKeyCombinationEvent event);

    @Override
    public void onInputEvent(AltKeyCombinationEvent event) {
        if (!isKeysCombination(event.getKeys())) {
            return;
        }

        onCombination(event);
    }

}
