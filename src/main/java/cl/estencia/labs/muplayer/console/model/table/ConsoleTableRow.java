package cl.estencia.labs.muplayer.console.model.table;

import cl.estencia.labs.muplayer.console.util.ConsoleUtil;
import cl.estencia.labs.muplayer.core.exception.MuPlayerException;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.*;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.LINE_BREAK_CHAR;
import static cl.estencia.labs.muplayer.console.common.enums.OutputType.info;
import static cl.estencia.labs.muplayer.console.model.table.ConsoleTable.DEFAULT_CONTENT_SIZE_LIMIT;

@Getter
@Setter
public class ConsoleTableRow {
    private final List<ConsoleTableCell> cells;
    private String color;
    private final int contentSizeLimit;

    public ConsoleTableRow() {
        this(null);
    }

    public ConsoleTableRow(String color, ConsoleTableCell... cells) {
        this(color, DEFAULT_CONTENT_SIZE_LIMIT);
        loadDefaultCells(cells);
    }

    public ConsoleTableRow(String color) {
        this(color, DEFAULT_CONTENT_SIZE_LIMIT);
    }

    public ConsoleTableRow(int contentSizeLimit) {
        this(null, contentSizeLimit);
    }

    public ConsoleTableRow(String color, int contentSizeLimit, ConsoleTableCell... cells) {
        this(color, contentSizeLimit);
        loadDefaultCells(cells);
    }

    public ConsoleTableRow(String color, int contentSizeLimit) {
        this.color = ConsoleUtil.isValidColor(color) ? color : ConsoleUtil.getOutputColor(info);
        this.cells = CollectionUtil.newFastArrayList();
        this.contentSizeLimit  = contentSizeLimit;
    }

    private void loadDefaultCells(ConsoleTableCell... cells) {
        if (cells == null || cells.length == 0) {
            return;
        }

        ConsoleTableCell cell;
        for (int i = 0; i < cells.length; i++) {
            cell = cells[i];
            if (!ConsoleUtil.isValidColor(cell.getColor())) {
                cell.setColor(color);
            }

            this.cells.add(cell);
        }
    }

    public void addCell(Object value) {
        if (value instanceof ConsoleTableCell) {
            throw new MuPlayerException("value object cannot be an instance of ConsoleTableCell");
        }

        addCell(value, color);
    }

    public void addCells(Object... values) {
        if (values == null || values.length == 0) {
            return;
        }

        for (int i = 0; i < values.length; i++) {
            addCell(values[i]);
        }
    }

    public void addCell(Object value, String color) {
        addCell(value, color, contentSizeLimit);
    }

    public void addCell(Object value, String color, int contentSizeLimit) {
        cells.add(new ConsoleTableCell(value, color, contentSizeLimit));
    }

    public ConsoleTableCell getCell(int columnIndex) {
        return cells.get(columnIndex);
    }

    public int getColumnsCount() {
        return cells.size();
    }

    public int getColumnLength(int index, Padding padding) {
        return cells.get(index).getLength(padding);
    }

    public void draw(StringBuilder sbTable, String tableColor, Padding padding,
                     List<Integer> biggerColumnLengths,
                     boolean useSingleLines, boolean useInternalLines,
                     int externalPadding) {

        boolean isValidTableColor = ConsoleUtil.isValidColor(this.color);
        String color = isValidTableColor
                ? this.color : (ConsoleUtil.isValidColor(tableColor)
                             ? tableColor : ConsoleUtil.getOutputColor(info));

        char borderChar = useSingleLines ? SINGLE_VERTICAL_LINE : DOUBLE_VERTICAL_LINE;
        char interColumnChar = useInternalLines
                ? (useSingleLines ? SINGLE_VERTICAL_LINE : DOUBLE_VERTICAL_LINE)
                : SPACE;

        sbTable.append(String.valueOf(SPACE).repeat(Math.max(0, externalPadding)));
        sbTable.append(color);
        sbTable.append(borderChar);

        int columnCount = biggerColumnLengths.size();
        int columnLength;
        int biggerColumnLength;
        int lenDiff;

        Padding lenPadding;
        ConsoleTableCell column;
        String coloredLine;
        for (int i = 0; i < columnCount; i++) {
            biggerColumnLength = biggerColumnLengths.get(i);
            column = getCell(i);
            columnLength = column.getLength(padding);
            lenDiff = biggerColumnLength - columnLength;
            lenPadding = new Padding(0, lenDiff, 0, 0);
            coloredLine = column.getColoredLineWithPadding(color, padding, lenPadding);

            sbTable.append(coloredLine);
            sbTable.append(interColumnChar);
        }

        if (isValidTableColor) {
            sbTable.deleteCharAt(sbTable.length() - 1);
            sbTable.append(tableColor);
            sbTable.append(borderChar);
        } else {
            sbTable.setCharAt(sbTable.length() - 1, borderChar);
        }

        sbTable.append(ConsoleUtil.getResetColor());
        sbTable.append(LINE_BREAK_CHAR);
    }

}
