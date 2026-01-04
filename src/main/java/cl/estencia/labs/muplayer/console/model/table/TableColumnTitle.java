package cl.estencia.labs.muplayer.console.model.table;

import cl.estencia.labs.muplayer.console.util.ConsoleUtil;
import cl.estencia.labs.muplayer.core.exception.MuPlayerException;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import lombok.Getter;

import java.util.List;

import static cl.estencia.labs.muplayer.console.common.ConsoleSymbols.*;
import static cl.estencia.labs.muplayer.console.common.ConsoleSymbols.LINE_BREAK_CHAR;
import static cl.estencia.labs.muplayer.console.common.enums.OutputType.info;

@Getter
public class TableColumnTitle {
    private final List<ConsoleTableCell> cells;
    private final String color;

    public TableColumnTitle(String color) {
        this.color = ConsoleUtil.isValidColor(color) ? color : ConsoleUtil.getOutputColor(info);
        this.cells = CollectionUtil.newFastArrayList();
    }

    public void addCell(Object value) {
        if (value instanceof ConsoleTableCell) {
            throw new MuPlayerException("value object cannot be an instance of ConsoleTableCell");
        }

        addCell(value, color);
    }

    public void addCell(Object value, String color) {
        cells.add(new ConsoleTableCell(value, color));
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

    public void draw(StringBuilder sbTable, Padding padding,
                     List<Integer> biggerColumnLengths,
                     boolean useSingleLines) {

        char borderChar = !useSingleLines ? SINGLE_VERTICAL_LINE : DOUBLE_VERTICAL_LINE;
        char interColumnChar = !useSingleLines ? SINGLE_VERTICAL_LINE : DOUBLE_VERTICAL_LINE;

        sbTable.append(getColor());
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
        sbTable.setCharAt(sbTable.length() - 1, borderChar);

        sbTable.append(ConsoleUtil.getResetColor());
        sbTable.append(LINE_BREAK_CHAR);
    }

}
