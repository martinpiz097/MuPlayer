package cl.estencia.labs.muplayer.console.unixconsole.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
@AllArgsConstructor
public class KeyInputEvent implements NativeInputEvent {
    private final int key;
    private final byte[] sequenceBytes;

    public char getKeyChar() {
        return (char) key;
    }

    @Override
    public String getInput() {
        return String.valueOf((char) key);
    }

    @Override
    public String toString() {
        return "KeyInputEvent{" +
                "key=" + key +
                ", keyChar=" + getKeyChar() +
                ", sequenceBytes=" + Arrays.toString(sequenceBytes) +
                '}';
    }
}
