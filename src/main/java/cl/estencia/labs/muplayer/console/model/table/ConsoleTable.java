package cl.estencia.labs.muplayer.console.model.table;

import cl.estencia.labs.muplayer.console.util.ConsoleUtil;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static cl.estencia.labs.muplayer.console.common.ConsoleSymbols.*;
import static cl.estencia.labs.muplayer.console.common.ConsoleSymbols.DOUBLE_BOTTOM_LEFT_CORNER;
import static cl.estencia.labs.muplayer.console.common.enums.OutputType.info;
import static cl.estencia.labs.muplayer.console.common.enums.OutputType.raw;

public class ConsoleTable {
    private final String title;
    private final String color;
    private final TableColumnTitle columnTitles;
    private final List<ConsoleTableRow> rows;
    private final Alignment alignment;
    private final Padding borderPadding;
    private final boolean useSingleLines;
    private final boolean useInternalLines;
    private final int contentSizeLimit;

    public static final byte DEFAULT_CONTENT_SIZE_LIMIT = 30;
    public static final String SHORT_VALUE_SUFFIX = "...";

    public ConsoleTable(String title) {
        this(title, ConsoleUtil.getOutputColor(info));
    }

    public ConsoleTable(String title, String color) {
        this(title, color, null, new Padding(0, 0, 0, 0));
    }

    public ConsoleTable(String title, String color, Alignment alignment, Padding borderPadding) {
        this(title, color, alignment, borderPadding, true, true);
    }

    public ConsoleTable(String title, String color, boolean useSingleLines, boolean useInternalLines) {
        this(title, color, null, new Padding(0, 0, 0, 0), useSingleLines, useInternalLines);
    }

    public ConsoleTable(String title, String color, Padding borderPadding, boolean useSingleLines, boolean useInternalLines) {
        this(title, color, null, borderPadding, useSingleLines, useInternalLines);
    }

    public ConsoleTable(String title, String color, Alignment alignment, Padding borderPadding, boolean useSingleLines, boolean useInternalLines) {
        this(title, color, alignment, borderPadding, useSingleLines, useInternalLines, DEFAULT_CONTENT_SIZE_LIMIT);
    }

    public ConsoleTable(String title, int contentSizeLimit) {
        this(title, ConsoleUtil.getOutputColor(info), contentSizeLimit);
    }

    public ConsoleTable(String title, String color, int contentSizeLimit) {
        this(title, color, null,
                new Padding(0, 0, 0, 0), contentSizeLimit);
    }

    public ConsoleTable(String title, String color, Alignment alignment, Padding borderPadding, int contentSizeLimit) {
        this(title, color, alignment, borderPadding, true, true, contentSizeLimit);
    }

    public ConsoleTable(String title, String color, boolean useSingleLines, boolean useInternalLines, int contentSizeLimit) {
        this(title, color, null, new Padding(0, 0, 0, 0),
                useSingleLines, useInternalLines, contentSizeLimit);
    }

    public ConsoleTable(String title, String color, Padding borderPadding, boolean useSingleLines,
                        boolean useInternalLines, int contentSizeLimit) {
        this(title, color, null, borderPadding, useSingleLines, useInternalLines, contentSizeLimit);
    }

    public ConsoleTable(String title, String color, Alignment alignment, Padding borderPadding,
                        boolean useSingleLines, boolean useInternalLines, int contentSizeLimit) {
        this.title = title;
        this.color = color;
        this.useInternalLines = useInternalLines;
        this.columnTitles = new TableColumnTitle(ConsoleUtil.getOutputColor(raw));
        this.rows = CollectionUtil.newFastArrayList();
        this.alignment = alignment != null ? alignment : Alignment.LEFT;
        this.borderPadding = borderPadding != null ? borderPadding : new Padding(0, 0, 0, 0);
        this.useSingleLines = useSingleLines;
        this.contentSizeLimit = contentSizeLimit;
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

    private List<Integer> getBiggerColumnLenghts(int columnsCount) {
        List<Integer> columnsBigLenghts = CollectionUtil.newFastArrayList();

        if (!rows.isEmpty()) {
            for (int i = 0; i < columnsCount; i++) {
                columnsBigLenghts.add(getBiggerCellLength(i));
            }
        } else {
            for (int i = 0; i < columnsCount; i++) {
                columnsBigLenghts.add(columnTitles.getCell(i).getLength(borderPadding));
            }
        }

        return columnsBigLenghts;
    }

    public int getWidth(List<Integer> biggerColumnLengths) {
        Integer biggerLenghtsSum = biggerColumnLengths.stream().reduce(Integer::sum).orElse(0);

        return biggerLenghtsSum * 2 + 1;
    }

    private void paintTableMargin(StringBuilder sbTable, String color, char startChar, char cellChar,
                                  char interColumnChar, char endChar,
                                  List<Integer> columnWidths, int columnsCount) {
        if (!ConsoleUtil.isValidColor(color)) {
            color = ConsoleUtil.getOutputColor(info);
        }

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

    private void paintTableTitleHeader(StringBuilder sbTable, String color,
                                       List<Integer> columnWidths, int columnsCount) {
        char headerChar = useSingleLines ? SINGLE_HORIZONTAL_LINE : DOUBLE_HORIZONTAL_LINE;

        paintTableMargin(sbTable, color, headerChar, headerChar,
                headerChar, headerChar, columnWidths, columnsCount);
    }

    private void paintTableTitle(StringBuilder sbTable, String color, List<Integer> columnWidths) {
        int tableWidth = getWidth(columnWidths);
        int widthDiff = tableWidth - title.length();
        int paddingLeft = widthDiff / 4;

        sbTable.append(color);
        sbTable.append(String.valueOf(SPACE).repeat(Math.max(0, paddingLeft)));
        sbTable.append(title);
        sbTable.append(ConsoleUtil.getResetColor());
        sbTable.append(LINE_BREAK_CHAR);
    }

    private void paintTableExternalMargin(StringBuilder sbTable, String color, List<Integer> columnWidths,
                                          boolean top, boolean useSingleLines, boolean useInternalLines,
                                          int columnsCount) {
        if (top) {
            char startChar = useSingleLines ? SINGLE_TOP_LEFT_CORNER : DOUBLE_TOP_LEFT_CORNER;
            char cellChar = useSingleLines ? SINGLE_HORIZONTAL_LINE : DOUBLE_HORIZONTAL_LINE;
            char interColumnChar = useInternalLines ?
                    (useSingleLines ? SINGLE_TOP_CORNER : DOUBLE_TOP_CORNER)
                    : cellChar;
            char endChar = useSingleLines ? SINGLE_TOP_RIGHT_CORNER : DOUBLE_TOP_RIGHT_CORNER;

            paintTableMargin(sbTable, color, startChar, cellChar,
                    interColumnChar, endChar, columnWidths, columnsCount);
        } else {
            char startChar = useSingleLines ? SINGLE_BOTTOM_LEFT_CORNER : DOUBLE_BOTTOM_LEFT_CORNER;
            char cellChar = useSingleLines ? SINGLE_HORIZONTAL_LINE : DOUBLE_HORIZONTAL_LINE;
            char interColumnChar = useInternalLines
                    ? (useSingleLines ? SINGLE_BOTTOM_CORNER : DOUBLE_BOTTOM_CORNER)
                    : cellChar;
            char endChar = useSingleLines ? SINGLE_BOTTOM_RIGHT_CORNER : DOUBLE_BOTTOM_RIGHT_CORNER;

            paintTableMargin(sbTable, color, startChar, cellChar,
                    interColumnChar, endChar, columnWidths, columnsCount);
        }
    }

    private void paintTableExternalMargin(StringBuilder sbTable, String color, List<Integer> columnWidths,
                                          boolean top, int columnsCount) {
        paintTableExternalMargin(sbTable, color, columnWidths, top,
                useSingleLines, useInternalLines, columnsCount);
    }

    private void paintInterMargin(StringBuilder sbTable, String color, List<Integer> columnWidths,
                                  boolean useSingleLines, boolean useInternalLines,
                                  int columnsCount) {
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

        paintTableMargin(sbTable, color, startChar, cellChar,
                interColumnChar, endChar, columnWidths, columnsCount);
    }

    private void paintPreTitlesMargin(StringBuilder sbTable, String color,
                                      List<Integer> columnWidths, int columnsCount) {

        boolean titlesWithSingleLine = !useInternalLines;
        char startChar = titlesWithSingleLine ? SINGLE_TOP_LEFT_CORNER : DOUBLE_TOP_LEFT_CORNER;
        char cellChar = titlesWithSingleLine ? SINGLE_HORIZONTAL_LINE : DOUBLE_HORIZONTAL_LINE;
        char interColumnChar = cellChar;
        char endChar = titlesWithSingleLine ? SINGLE_TOP_RIGHT_CORNER : DOUBLE_TOP_RIGHT_CORNER;

        paintTableMargin(sbTable, color, startChar, cellChar,
                interColumnChar, endChar, columnWidths, columnsCount);
    }

    private void paintPostTitlesMargin(StringBuilder sbTable, String color,
                                       List<Integer> columnWidths, int columnsCount) {

        boolean titlesWithSingleLine = !useInternalLines;
        char startChar = titlesWithSingleLine ? SINGLE_BOTTOM_LEFT_CORNER : DOUBLE_BOTTOM_LEFT_CORNER;
        char cellChar = titlesWithSingleLine ? SINGLE_HORIZONTAL_LINE : DOUBLE_HORIZONTAL_LINE;
        char interColumnChar = cellChar;
        char endChar = titlesWithSingleLine ? SINGLE_BOTTOM_RIGHT_CORNER : DOUBLE_BOTTOM_RIGHT_CORNER;

        paintTableMargin(sbTable, color, startChar, cellChar,
                interColumnChar, endChar, columnWidths, columnsCount);
    }

    private void paintInterRowMargin(StringBuilder sbTable, List<Integer> columnWidths, int columnsCount) {
        paintInterMargin(sbTable, color, columnWidths, useSingleLines, useInternalLines, columnsCount);
    }

    public ConsoleTableRow addRow(String color) {
        return addRow(color, DEFAULT_CONTENT_SIZE_LIMIT);
    }

    public ConsoleTableRow addRow(String color, int contentSizeLimit) {
        return addRow(color, contentSizeLimit, null);
    }

    public ConsoleTableRow addRowWithCells(ConsoleTableCell... cells) {
        return addRow(color, contentSizeLimit, cells);
    }

    public ConsoleTableRow addRow(String color, int contentSizeLimit, ConsoleTableCell... cells) {
        ConsoleTableRow row = new ConsoleTableRow(color, contentSizeLimit, cells);
        return addRow(row);
    }

    public ConsoleTableRow addRow(ConsoleTableRow row) {
        rows.add(row);
        return row;
    }

    public int getColumnTitlesCount() {
        return columnTitles.getColumnsCount();
    }

    public int getRowsColumnCount() {
        Optional<ConsoleTableRow> maxCount = rows.parallelStream()
                .max(Comparator.comparingInt(ConsoleTableRow::getColumnsCount));

        return maxCount.map(ConsoleTableRow::getColumnsCount).orElse(0);
    }

    public int getRowsCount() {
        return rows.size();
    }

    public void addColumn(String name) {
        columnTitles.addCell(name);
    }

    public String draw() {
        StringBuilder sbTable = new StringBuilder();
        int rowsCount = getRowsCount();
        int columnsCount = getRowsColumnCount();

        List<Integer> biggerColumnLenghts = getBiggerColumnLenghts(columnsCount);
        ConsoleTableRow row;

        if (title != null && !title.isBlank()) {
            String headerColor = ConsoleUtil.getOutputColor(raw);

            paintTableTitleHeader(sbTable, headerColor,biggerColumnLenghts, columnsCount);
            paintTableTitle(sbTable, headerColor, biggerColumnLenghts);
            paintTableTitleHeader(sbTable, headerColor,biggerColumnLenghts, columnsCount);
        }

        String titlesColor = columnTitles.getColor();
        if (columnTitles.getColumnsCount() > 0) {
            paintPreTitlesMargin(sbTable, titlesColor, biggerColumnLenghts, columnsCount);
            columnTitles.draw(sbTable, borderPadding, biggerColumnLenghts, useSingleLines);
            paintPostTitlesMargin(sbTable, titlesColor, biggerColumnLenghts, columnsCount);
        }

        if (!rows.isEmpty()) {
            paintTableExternalMargin(sbTable, color, biggerColumnLenghts, true, columnsCount);
            for (int i = 0; i < rowsCount; i++) {
                row = rows.get(i);
                row.draw(sbTable, borderPadding, biggerColumnLenghts,
                        useSingleLines, useInternalLines);
                if (useInternalLines && i < rowsCount - 1) {
                    paintInterRowMargin(sbTable, biggerColumnLenghts, columnsCount);
                }
            }
            paintTableExternalMargin(sbTable, color, biggerColumnLenghts, false, columnsCount);

            sbTable.append(LINE_BREAK_CHAR);
        }

        if (columnTitles.getColumnsCount() > 0) {
            sbTable.append(" Total: " + getRowsCount());
            sbTable.append(LINE_BREAK_CHAR);
        }

        return sbTable.toString();
    }

}
