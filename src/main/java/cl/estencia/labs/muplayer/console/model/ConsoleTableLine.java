package cl.estencia.labs.muplayer.console.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.orangelogger.sys.ConsoleColor;

import static cl.estencia.labs.muplayer.console.common.ConsoleSymbols.DEFAULT_VALUES_SEPARATOR;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ConsoleTableLine {
    private final String title;
    private final Object value;
    private final String separator;
    private final String titleColor;
    private final String valueColor;

    public ConsoleTableLine(String title, Object value) {
        this(title, value, DEFAULT_VALUES_SEPARATOR);
    }

    public ConsoleTableLine(String title, Object value, String separator) {
        this(title, value, separator, null, null);
    }

    private boolean isValidColor(String color) {
        return color != null
                && !color.isBlank()
                && !color.trim().equalsIgnoreCase("null");
    }

    public String getRawLine() {
        return title + separator + value.toString();
    }

    public int getRawLineLength() {
        return getRawLine().length();
    }

    public String getCompleteLine() {
        final String completeTitle = isValidColor(titleColor)
                ? titleColor + title + ConsoleColor.ANSI_RESET
                : title;

        final String completeSeparator = isValidColor(titleColor)
                ? titleColor + separator + ConsoleColor.ANSI_RESET
                : separator;

        final String completeValue = isValidColor(valueColor)
                ? valueColor + value + ConsoleColor.ANSI_RESET
                : value.toString();

        return completeTitle + completeSeparator + completeValue;
    }

}
