package org.muplayer.main;

import java.util.LinkedList;
import java.util.List;

public class ConsoleManager {
    private String currentCmd;
    private List<String> listCommands;

    public ConsoleManager() {
        currentCmd = null;
        listCommands = new LinkedList<>();
    }

    public String getCurrentCmd() {
        return currentCmd;
    }

    public void setCurrentCmd(String currentCmd) {
        this.currentCmd = currentCmd;
    }

    public List<String> getListCommands() {
        return listCommands;
    }

    public void addCommand(String cmd) {
        listCommands.add(cmd);
    }




}
