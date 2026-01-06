package cl.estencia.labs.muplayer.console.unix.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class KeyInputEvent extends NativeInputEvent {
    private final int key;
    private final byte[] sequenceBytes;

    @Override
    public char nextChar() {
        return (char) key;
    }

    @Override
    public String getLine() {
        return String.valueOf(key);
    }

}
