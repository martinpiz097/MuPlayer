package cl.estencia.labs.muplayer.console.unixconsole;

import lombok.Getter;

import static cl.estencia.labs.muplayer.console.unixconsole.InputMode.COMMANDS;
import static cl.estencia.labs.muplayer.console.unixconsole.InputMode.SINGLE_SHORCUTS;

@Getter
public class InputConfig {
    private volatile InputMode inputMode;
    private volatile boolean inputBlocked;
    private final int toggleModeKey;
    private final int unlockKey;

    public InputConfig(InputMode inputMode, boolean inputBlocked, int toggleModeKey, int unlockKey) {
        this.inputMode = inputMode;
        this.inputBlocked = inputBlocked;
        this.toggleModeKey = toggleModeKey;
        this.unlockKey = unlockKey;
    }

    public synchronized void toggleInputMode() {
        setInputMode(inputMode == SINGLE_SHORCUTS ? COMMANDS : SINGLE_SHORCUTS);
    }

    public synchronized void toggleInputBlocked() {
        setInputBlocked(!inputBlocked);
    }

    public synchronized void setInputMode(InputMode inputMode) {
        this.inputMode = inputMode;
    }

    public synchronized void setInputBlocked(boolean inputBlocked) {
        this.inputBlocked = inputBlocked;
    }

}
