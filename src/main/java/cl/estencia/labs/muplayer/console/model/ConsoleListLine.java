package cl.estencia.labs.muplayer.console.model;

import cl.estencia.labs.muplayer.console.util.ConsoleUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.orangelogger.sys.ConsoleColor;

import static cl.estencia.labs.muplayer.console.common.ConsoleSymbols.DEFAULT_LIST_VALUES_SEPARATOR;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ConsoleListLine {
    private final String title;
    private final Object value;
    private final String separator;
    private final String titleColor;
    private final String valueColor;

    public ConsoleListLine(String title, Object value) {
        this(title, value, DEFAULT_LIST_VALUES_SEPARATOR);
    }

    public ConsoleListLine(String title, Object value, String separator) {
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
                ? titleColor + title + ConsoleUtil.getResetColor()
                : title;

        final String completeSeparator = isValidColor(titleColor)
                ? titleColor + separator + ConsoleUtil.getResetColor()
                : separator;

        final String completeValue = isValidColor(valueColor)
                ? valueColor + value + ConsoleUtil.getResetColor()
                : value.toString();

        return completeTitle + completeSeparator + completeValue;
    }

}
