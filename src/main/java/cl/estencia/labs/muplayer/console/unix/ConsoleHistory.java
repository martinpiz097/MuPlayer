package cl.estencia.labs.muplayer.console.unix;

import cl.estencia.labs.muplayer.core.util.CollectionUtil;

import java.util.List;

public class ConsoleHistory {
    private final List<String> commands;
    private volatile int currentIndex;

    public ConsoleHistory() {
        this.commands = CollectionUtil.newFastArrayList();
    }

    public void addCommand(String cmd) {
        if (cmd == null || cmd.isBlank()) {
            return;
        }

        commands.add(cmd.trim());
        currentIndex = commands.size();
    }

    private void changeIndex(boolean next) {
        if (next) {
            currentIndex = Math.min(currentIndex + 1, commands.size() - 1);
        } else {
            currentIndex = Math.max(currentIndex - 1, 0);
        }
    }

    public String getPrevCommand() {
        if (commands.isEmpty()) {
            return "";
        }

        changeIndex(false);
        return commands.get(currentIndex);
    }

    public String getNextCommand() {
        if (commands.isEmpty()) {
            return "";
        }

        changeIndex(true);
        return commands.get(currentIndex);
    }

}
