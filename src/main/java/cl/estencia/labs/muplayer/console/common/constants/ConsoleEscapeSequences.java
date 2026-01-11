package cl.estencia.labs.muplayer.console.common.constants;

import static cl.estencia.labs.muplayer.console.common.constants.KeyCodes.ESC;
import static cl.estencia.labs.muplayer.console.common.constants.KeyCodes.BACKSPACE;
import static cl.estencia.labs.muplayer.console.common.constants.KeyCodes.SPACE;
import static cl.estencia.labs.muplayer.console.common.constants.KeyCodes.toChar;

public class ConsoleEscapeSequences {
    // setup para que definir un foreground con true color (RGB completo)
    public static final String SETUP_FG_RGB_TRUE_COLOR = toChar(ESC) + "[38;2;";
    public static final String SETUP_BG_RGB_TRUE_COLOR = toChar(ESC) + "[48;2;";
    public static final String FULL_RESET = toChar(ESC) + "[0m";

    public static String partialCartReturn(int columns) {
        if (columns < 1) {
            return "";
        }

        return String.valueOf(toChar(BACKSPACE)).repeat(columns);
    }

    public static String padding(int count) {
        if (count < 1) {
            return "";
        }

        return String.valueOf(toChar(SPACE)).repeat(count);
    }

}
