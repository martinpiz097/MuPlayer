package cl.estencia.labs.muplayer.console.unix.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import static cl.estencia.labs.aucom.common.IOConstants.EOF;

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
