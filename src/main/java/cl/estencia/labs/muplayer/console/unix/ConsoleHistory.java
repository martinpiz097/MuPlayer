package cl.estencia.labs.muplayer.console.unix;

import cl.estencia.labs.muplayer.core.util.CollectionUtil;

import java.util.List;

import static cl.estencia.labs.muplayer.core.log.ConsolePrinter.infoLine;

public class ConsoleHistory {
    private final List<String> commands;
    private volatile int currentCommandIndex;

    public ConsoleHistory() {
        this.commands = CollectionUtil.newFastArrayList();
        this.currentCommandIndex = 0;
    }

    public void addCommand(String cmd) {
        if (cmd == null || cmd.isBlank()) {
            return;
        }

        commands.add(cmd.trim());
        currentCommandIndex = commands.size() - 1;
    }

    public void changeIndex(boolean next) {
        infoLine(commands);
        if (next) {
            currentCommandIndex = Math.min(currentCommandIndex + 1, commands.size() - 1);
            infoLine("("+currentCommandIndex+"/"+(commands.size()-1)+"; "+commands.size()+") next index");
        } else {
            currentCommandIndex = Math.max(currentCommandIndex - 1, 0);
            infoLine("("+currentCommandIndex+"/"+(commands.size()-1)+"; "+commands.size()+") prev index");
        }

        infoLine(commands.get(currentCommandIndex));
        infoLine("-------------------");
    }

    public void updateCommand(int index, String cmd) {
        if (commands.isEmpty()) {
            addCommand(cmd);
            return;
        }
        if (cmd == null || cmd.isBlank()) {
            return;
        }

        commands.set(index, cmd.trim());
    }

    public void updateLastCommand(String cmd) {
        updateCommand(commands.size() - 1, cmd);
    }

    public void updateCurrentCommand(String cmd) {
        int normalizedCommandIndex = Math.min(Math.max(0, currentCommandIndex), commands.size() - 1);
        updateCommand(normalizedCommandIndex, cmd);
    }

    public String getPrevCommand() {
        if (commands.isEmpty()) {
            return null;
        }

        changeIndex(false);
        return commands.get(currentCommandIndex);
    }

    public String getNextCommand() {
        if (commands.isEmpty()) {
            return null;
        }

        changeIndex(true);
        return commands.get(currentCommandIndex);
    }

}
