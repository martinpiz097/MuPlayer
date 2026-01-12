package cl.estencia.labs.muplayer.console.unix.listener;

import cl.estencia.labs.muplayer.console.unix.event.KeyInputEvent;

public abstract class KeyInputListener implements NativeInputListener<KeyInputEvent> {
    protected final char[] charCodes;

    public KeyInputListener(char... charCodes) {
        this.charCodes = charCodes != null ? charCodes : new char[]{};
    }

    public KeyInputListener(int... charCodes) {
        this.charCodes = createCharArrayFromInts(charCodes);
    }

    private char[] createCharArrayFromInts(int... intArray) {
        if (intArray == null || intArray.length == 0) {
            return new char[]{};
        }

        int length = intArray.length;
        char[] charArray = new char[length];
        for (int i = 0; i < length; i++) {
            charArray[i] = (char) intArray[i];
        }

        return charArray;
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
