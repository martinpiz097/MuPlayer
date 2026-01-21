package cl.estencia.labs.muplayer.console.unix.listener;

import cl.estencia.labs.muplayer.console.unix.event.KeyInputEvent;

import static cl.estencia.labs.muplayer.core.util.IOUtil.createCharArrayFromInts;

public abstract class KeyInputListener implements NativeInputListener<KeyInputEvent> {
    protected final char[] charCodes;

    public KeyInputListener(char... charCodes) {
        this.charCodes = charCodes != null ? charCodes : new char[]{};
    }

    public KeyInputListener(int... charCodes) {
        this.charCodes = createCharArrayFromInts(charCodes);
    }

    public boolean isGeneric() {
        return charCodes.length == 0;
    }

    public boolean hasKey(int key) {
        int charCodesLength = this.charCodes.length;
        for (int i = 0; i < charCodesLength; i++) {
            if (charCodes[i] == key) {
                return true;
            }
        }

        return false;
    }

    public abstract void onInputEvent(KeyInputEvent event);

}
