package cl.estencia.labs.muplayer.console.unix.event;

import cl.estencia.labs.muplayer.core.util.IOUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class AltKeyCombinationEvent implements NativeInputEvent {
    private final int[] keys;

    public AltKeyCombinationEvent(int[] keys) {
        this.keys = keys;
    }

    public AltKeyCombinationEvent(byte[] keys) {
        this.keys = IOUtil.createIntArrayFromBytes(keys);
    }

    @Override
    public String getInput() {
        return "";
    }

    @Override
    public String toString() {
        return "KeyInputEvent{" +
                "keys=" + Arrays.toString(keys) +
                '}';
    }
}
