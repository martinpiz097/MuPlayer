package cl.estencia.labs.muplayer.console.model.table;

import cl.estencia.labs.muplayer.console.util.ConsoleUtil;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;

import java.util.List;

import static cl.estencia.labs.muplayer.console.common.ConsoleSymbols.*;
import static cl.estencia.labs.muplayer.console.common.ConsoleSymbols.DOUBLE_BOTTOM_LEFT_CORNER;
import static cl.estencia.labs.muplayer.console.common.enums.OutputType.info;

public class ConsoleTable {
    private final String title;
    private final String color;
    private final List<String> columnNames;
    private final List<ConsoleTableRow> rows;
    private final Padding borderPadding;
    private final boolean useSingleLines;
    private final boolean useInternalLines;

    public ConsoleTable(String title) {
        this(title, ConsoleUtil.getOutputColor(info));
    }

    public ConsoleTable(String title, String color) {
        this(title, color, new Padding(0, 0, 0, 0));
    }

    public ConsoleTable(String title, String color, Padding borderPadding) {
        this(title, color, borderPadding, true, true);
    }

    public ConsoleTable(String title, String color, boolean useSingleLines, boolean useInternalLines) {
        this(title, color, new Padding(0, 0, 0, 0), useSingleLines, useInternalLines);
    }

    public ConsoleTable(String title, String color, Padding borderPadding, boolean useSingleLines, boolean useInternalLines) {
        this.title = title;
        this.color = color;
        this.useInternalLines = useInternalLines;
        this.columnNames = CollectionUtil.newFastArrayList();
        this.rows = CollectionUtil.newFastArrayList();
        this.borderPadding = borderPadding != null ? borderPadding : new Padding(0, 0, 0, 0);
        this.useSingleLines = useSingleLines;
    }

    private char getCellSeparator() {
        return useSingleLines ? SINGLE_INTERNAL_CORNER : DOUBLE_INTERNAL_CORNER;
    }

    private int getBiggerCellLength(int columnIndex) {
        return rows.parallelStream()
                .map(row -> row.getCell(columnIndex)
                        .getLength(borderPadding))
                .max(Integer::compareTo)
                .orElse(0);
    }

    private List<Integer> getBiggerColumnLenghts() {
        List<Integer> columnsBigLenghts = CollectionUtil.newFastArrayList();

        int columnsCount = getColumnsCount();
        for (int i = 0; i < columnsCount; i++) {
            columnsBigLenghts.add(getBiggerCellLength(i));
        }

        return columnsBigLenghts;
    }

    private void paintTableMargin(StringBuilder sbTable, String color, char startChar, char cellChar,
                                  char interColumnChar, char endChar,
                                  List<Integer> columnWidths) {
        if (!ConsoleUtil.isValidColor(color)) {
            color = ConsoleUtil.getOutputColor(info);
        }

        int columnsCount = getColumnsCount();
        int columnWidth;

        sbTable.append(color);
        sbTable.append(startChar);
        for (int i = 0; i < columnsCount; i++) {
            columnWidth = columnWidths.get(i);
            sbTable.append(String.valueOf(cellChar).repeat(Math.max(0, columnWidth)));
            sbTable.append(interColumnChar);

        }
        sbTable.setCharAt(sbTable.length() - 1, endChar);

        sbTable.append(ConsoleUtil.getResetColor());
        sbTable.append(LINE_BREAK_CHAR);
    }

    private void paintTableTitleHeader(StringBuilder sbTable, List<Integer> columnWidths) {
        char headerChar = useSingleLines ? SINGLE_HORIZONTAL_LINE : DOUBLE_HORIZONTAL_LINE;

        paintTableMargin(sbTable, color, headerChar, headerChar, headerChar, headerChar, columnWidths);
    }

    private void paintTableExternalMargin(StringBuilder sbTable, List<Integer> columnWidths, boolean top) {
        if (top) {
            char startChar = useSingleLines ? SINGLE_TOP_LEFT_CORNER : DOUBLE_TOP_LEFT_CORNER;
            char cellChar = useSingleLines ? SINGLE_HORIZONTAL_LINE : DOUBLE_HORIZONTAL_LINE;
            char interColumnChar = useInternalLines ?
                    (useSingleLines ? SINGLE_TOP_CORNER : DOUBLE_TOP_CORNER)
                    : cellChar;
            char endChar = useSingleLines ? SINGLE_TOP_RIGHT_CORNER : DOUBLE_TOP_RIGHT_CORNER;

            paintTableMargin(sbTable, color, startChar, cellChar, interColumnChar, endChar, columnWidths);
        } else {
            char startChar = useSingleLines ? SINGLE_BOTTOM_LEFT_CORNER : DOUBLE_BOTTOM_LEFT_CORNER;
            char cellChar = useSingleLines ? SINGLE_HORIZONTAL_LINE : DOUBLE_HORIZONTAL_LINE;
            char interColumnChar = useInternalLines
                    ? (useSingleLines ? SINGLE_BOTTOM_CORNER : DOUBLE_BOTTOM_CORNER)
                    : cellChar;
            char endChar = useSingleLines ? SINGLE_BOTTOM_RIGHT_CORNER : DOUBLE_BOTTOM_RIGHT_CORNER;

            paintTableMargin(sbTable, color, startChar, cellChar, interColumnChar, endChar, columnWidths);
        }
    }

    private void paintInterRowMargin(StringBuilder sbTable, List<Integer> columnWidths) {
        char startChar = useInternalLines
                ? (useSingleLines ? SINGLE_LEFT_UNION : DOUBLE_RIGHT_UNION)
                : (useSingleLines ? SINGLE_VERTICAL_LINE : DOUBLE_VERTICAL_LINE);
        char cellChar = useInternalLines
                ? (useSingleLines ? SINGLE_HORIZONTAL_LINE : DOUBLE_HORIZONTAL_LINE)
                : SPACE;
        char interColumnChar = useInternalLines
                ? (useSingleLines ? SINGLE_INTERNAL_CORNER : DOUBLE_INTERNAL_CORNER)
                : SPACE;
        char endChar = useInternalLines
                ? (useSingleLines ? SINGLE_RIGHT_UNION : DOUBLE_RIGHT_UNION)
                : (useSingleLines ? SINGLE_VERTICAL_LINE : DOUBLE_VERTICAL_LINE);

        paintTableMargin(sbTable, color, startChar, cellChar, interColumnChar, endChar, columnWidths);
    }

    public int getColumnsCount() {
        return columnNames.size();
    }

    public int getRowsCount() {
        return rows.size();
    }

    public void addColumn(String name) {
        columnNames.add(name);
    }

    public void addRow(ConsoleTableRow row) {
        rows.add(row);
    }

    public String draw() {
        StringBuilder sbTable = new StringBuilder();
        int rowsCount = getRowsCount();
        List<Integer> biggerColumnLenghts = getBiggerColumnLenghts();
        ConsoleTableRow row;

        if (title != null && !title.isBlank()) {
            paintTableTitleHeader(sbTable, biggerColumnLenghts);
            sbTable.append(SPACE).append(title).append(LINE_BREAK_CHAR);
        }

        paintTableExternalMargin(sbTable, biggerColumnLenghts, true);
        for (int i = 0; i < rowsCount; i++) {
            row = rows.get(i);
            row.draw(sbTable, borderPadding, biggerColumnLenghts,
                    useSingleLines, useInternalLines);
            if (i < rowsCount - 1) {
                paintInterRowMargin(sbTable, biggerColumnLenghts);
            }
        }

        paintTableExternalMargin(sbTable, biggerColumnLenghts, false);
        sbTable.append(LINE_BREAK_CHAR);

        return sbTable.toString();
    }

}
