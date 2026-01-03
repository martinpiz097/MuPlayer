package cl.estencia.labs.muplayer.console.model.table;

import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import lombok.Getter;

import java.util.List;

@Getter
public class ConsoleTableRow {
    private final List<ConsoleTableCell> cells;

    public ConsoleTableRow() {
        this.cells = CollectionUtil.newFastArrayList();
    }

    public void addCell(ConsoleTableCell cell) {
        cells.add(cell);
    }

    public ConsoleTableCell getCell(int columnIndex) {
        return cells.get(columnIndex);
    }

    public int getColumnLength(int index, Padding padding) {
        return cells.get(index).getLength(padding);
    }

    public String getColumnFullContent(int index, Padding cellPadding, Padding extraPadding) {
        return cells.get(index).getColoredLineWithPadding(cellPadding, extraPadding);
    }

}
