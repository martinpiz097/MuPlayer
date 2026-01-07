package cl.estencia.labs.muplayer.console.unix.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import static cl.estencia.labs.aucom.common.IOConstants.EOF;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.LINE_BREAK;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.LINE_BREAK_CHAR;

@Getter
@Setter
@AllArgsConstructor
public class LineInputEvent extends NativeInputEvent {
    private final StringBuilder sbInput;
    private int charIterator;

    public LineInputEvent(String input) {
        this.sbInput = new StringBuilder(input);
        this.charIterator = 0;
    }

    public boolean isEmptyLine() {
        return sbInput.isEmpty() || sbInput.toString().isBlank()
                || (isOnlyBackspace());
    }

    public boolean isOnlyBackspace() {
        return !sbInput.isEmpty() && sbInput.toString().trim().equals(LINE_BREAK);
    }

    @Override
    public char nextChar() {
        if (charIterator == sbInput.length()) {
            return (char) EOF;
        }

        return sbInput.charAt(charIterator ++);
    }

    @Override
    public String getLine() {
        return sbInput.toString();
    }

}
