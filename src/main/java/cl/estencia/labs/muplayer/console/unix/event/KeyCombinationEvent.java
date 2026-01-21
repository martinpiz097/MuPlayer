package cl.estencia.labs.muplayer.console.unix.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
@AllArgsConstructor
public class KeyCombinationEvent implements NativeInputEvent {
    private final int[] keys;

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
