package cl.estencia.labs.muplayer.console.unix;

import cl.estencia.labs.muplayer.console.common.constants.KeyCodes;
import lombok.Getter;

import static cl.estencia.labs.muplayer.console.common.constants.KeyCodes.TAB;
import static cl.estencia.labs.muplayer.console.unix.InputMode.SINGLE_SHORCUTS;
import static cl.estencia.labs.muplayer.console.unix.InputMode.COMMANDS;

@Getter
public class InputModeConfig {
    private volatile InputMode inputMode;
    private final int toggleModeKey;
//    private final int wordsModeKey;

    public InputModeConfig() {
        this(COMMANDS);
    }

    public InputModeConfig(InputMode inputMode) {
        this(inputMode, TAB);
    }

    public InputModeConfig(InputMode inputMode, int toggleModeKey) {
        this.inputMode = inputMode;
        this.toggleModeKey = toggleModeKey;
    }

    public synchronized void toggleInputMode() {
        setInputMode(inputMode == SINGLE_SHORCUTS ? COMMANDS : SINGLE_SHORCUTS);
    }

    public synchronized void setInputMode(InputMode inputMode) {
        this.inputMode = inputMode;
    }

}
