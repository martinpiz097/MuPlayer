package cl.estencia.labs.muplayer.console.unix.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import static cl.estencia.labs.muplayer.core.aucom.common.IOConstants.EOF;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.LINE_BREAK;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.LINE_BREAK_CHAR;

@Getter
@Setter
@AllArgsConstructor
public class LineInputEvent implements NativeInputEvent {
    private final StringBuilder sbInput;

    public LineInputEvent(String input) {
        this.sbInput = new StringBuilder(input);
    }

    public boolean isEmptyLine() {
        return sbInput.isEmpty() || sbInput.toString().isBlank()
                || (isOnlyBackspace());
    }

    public boolean isOnlyBackspace() {
        return !sbInput.isEmpty() && sbInput.toString().trim().equals(LINE_BREAK);
    }

    @Override
    public String getInput() {
        return sbInput.toString();
    }

}
