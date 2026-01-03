package cl.estencia.labs.muplayer.console.model.table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.orangelogger.sys.ConsoleColor;

import static cl.estencia.labs.muplayer.console.common.ConsoleSymbols.SPACE;

@AllArgsConstructor
@Getter
public class ConsoleTableCell {
    private final Object value;
    private final String color;

    public ConsoleTableCell(Object value) {
        this.value = value;
        this.color = null;
    }

    private boolean isValidColor(String color) {
        return color != null
                && !color.isBlank()
                && !color.trim().equalsIgnoreCase("null");
    }

    private void appendPadding(StringBuilder stringBuilder, int padding) {
        if (padding < 1) {
            return;
        }

        for (int i = 0; i < padding; i++) {
            stringBuilder.append(SPACE);
        }
    }

    public String getColoredLineWithPadding(Padding cellPadding, Padding extraPadding) {
        if (cellPadding == null) {
            cellPadding = new Padding(0, 0, 0, 0);
        }
        if (extraPadding == null) {
            extraPadding = new Padding(0, 0, 0, 0);
        }

        String coloredLine = isValidColor(color)
                ? color + value + ConsoleColor.ANSI_RESET
                : value.toString();

        StringBuilder sbLine = new StringBuilder();


        appendPadding(sbLine, cellPadding.left());
        sbLine.append(coloredLine);
        appendPadding(sbLine, cellPadding.right());
        appendPadding(sbLine, extraPadding.right());

        return sbLine.toString();
    }

    public int getLength(Padding padding) {
        if (padding == null) {
            return value.toString().length();
        }

        return padding.left() + value.toString().length() + padding.right();
    }

}
