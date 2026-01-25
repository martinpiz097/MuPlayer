package cl.estencia.labs.muplayer.console.model.table;

import cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols;
import cl.estencia.labs.muplayer.console.util.ConsoleUtil;
import lombok.Getter;
import lombok.Setter;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.SPACE_CHAR;
import static cl.estencia.labs.muplayer.console.common.enums.OutputLevel.info;
import static cl.estencia.labs.muplayer.console.model.table.ConsoleTable.DEFAULT_CONTENT_SIZE_LIMIT;
import static cl.estencia.labs.muplayer.core.util.StringUtils.reduceString;

@Getter
@Setter
public class ConsoleTableCell {
    private final String value;
    private String color;
    private final int contentSizeLimit;

    public ConsoleTableCell(Object value) {
        this(value, ConsoleUtil.getColorFromLevel(info), DEFAULT_CONTENT_SIZE_LIMIT);
    }

    public ConsoleTableCell(Object value, String color) {
        this(value, color, DEFAULT_CONTENT_SIZE_LIMIT);
    }

    public ConsoleTableCell(Object value, int contentSizeLimit) {
        this(value, ConsoleUtil.getColorFromLevel(info), contentSizeLimit);
    }

    public ConsoleTableCell(Object value, String color, int contentSizeLimit) {
        this.value = normalizedValue(value, contentSizeLimit);
        this.color = color;
        this.contentSizeLimit = contentSizeLimit;
    }

    private String replaceInvalidChars(String str) {
        return str.replace('｜', ConsoleSymbols.PIPE);
    }

    private String normalizedValue(Object originalValue, int contentSizeLimit) {
        if (originalValue == null) {
            return "";
        }

        String strValue = originalValue.toString();
        String reducedValue = reduceString(strValue, contentSizeLimit);

        return replaceInvalidChars(reducedValue);
    }

    private void appendPadding(StringBuilder stringBuilder, int padding) {
        if (padding < 1) {
            return;
        }

        for (int i = 0; i < padding; i++) {
            stringBuilder.append(SPACE_CHAR);
        }
    }

    public String getColoredLineWithPadding(String rowColor, Padding cellPadding, Padding extraPadding) {
        String color = ConsoleUtil.isValidColor(this.color)
                ? this.color : (ConsoleUtil.isValidColor(rowColor)
                ? rowColor : ConsoleUtil.getColorFromLevel(info));

        if (cellPadding == null) {
            cellPadding = new Padding(0, 0, 0, 0);
        }
        if (extraPadding == null) {
            extraPadding = new Padding(0, 0, 0, 0);
        }

        String coloredLine = ConsoleUtil.coloredString(value, color, false);
        StringBuilder sbLine = new StringBuilder();

        appendPadding(sbLine, cellPadding.left());
        sbLine.append(coloredLine);

        appendPadding(sbLine, cellPadding.right());
        appendPadding(sbLine, extraPadding.right());

        return sbLine.toString();
    }

    public int getLength(Padding padding) {
        int valueLength = value.length();

        return padding != null ? padding.left() + valueLength + padding.right() : valueLength;
    }

}
