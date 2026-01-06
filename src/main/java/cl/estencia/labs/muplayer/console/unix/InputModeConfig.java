package cl.estencia.labs.muplayer.console.unix;

import cl.estencia.labs.muplayer.console.common.constants.KeyCodes;
import lombok.Getter;

import static cl.estencia.labs.muplayer.console.unix.InputMode.SINGLE_CHAR;
import static cl.estencia.labs.muplayer.console.unix.InputMode.WORDS;

@Getter
public class InputModeConfig {
    private volatile InputMode inputMode;
    private final int toggleModeKey;
//    private final int wordsModeKey;

    public InputModeConfig() {
        this(WORDS);
    }

    public InputModeConfig(InputMode inputMode) {
        this.inputMode = inputMode;
        this.toggleModeKey = KeyCodes.TAB;
    }

    public synchronized void toggleInputMode() {
        setInputMode(inputMode == SINGLE_CHAR ? WORDS : SINGLE_CHAR);
    }

    public synchronized void setInputMode(InputMode inputMode) {
        this.inputMode = inputMode;
    }

}
