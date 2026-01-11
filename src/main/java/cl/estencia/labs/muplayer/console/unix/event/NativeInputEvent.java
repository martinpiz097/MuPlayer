package cl.estencia.labs.muplayer.console.unix.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class NativeInputEvent {
    public abstract String getInput();
}
