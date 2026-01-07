package cl.estencia.labs.muplayer.console.unix;

import cl.estencia.labs.muplayer.core.util.CollectionUtil;

import java.util.List;

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
        currentCommandIndex = commands.size();
    }

    public void changeIndex(boolean next) {
        if (next) {
            currentCommandIndex = Math.min(currentCommandIndex + 1, commands.size() - 1);
        } else {
            currentCommandIndex = Math.max(currentCommandIndex - 1, 0);
        }
    }

    public void updateLastCommand(String cmd) {
        if (commands.isEmpty()) {
            addCommand(cmd);
            return;
        }
        if (cmd == null || cmd.isBlank()) {
            return;
        }

        commands.set(commands.size() - 1, cmd.trim());
    }

    public String getPrevCommand() {
        if (commands.isEmpty()) {
            return "";
        }

        changeIndex(false);
        return commands.get(currentCommandIndex);
    }

    public String getNextCommand() {
        if (commands.isEmpty()) {
            return "";
        }

        changeIndex(true);
        return commands.get(currentCommandIndex);
    }

}
