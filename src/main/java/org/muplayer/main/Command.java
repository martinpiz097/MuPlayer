package org.muplayer.main;

import java.util.Arrays;

public class Command {
    private final String order;
    private final String[] options;

    public Command(String order, String... options) {
        this.order = order;
        this.options = options;
    }

    public Command(String strCmd) {
        String[] splitcmd = strCmd.split(" ");
        order = splitcmd[0];
        if (splitcmd.length > 1)
            options = Arrays.copyOfRange(splitcmd, 1, splitcmd.length);
        else
            options = null;
    }

    public boolean hasOptions() {
        return options != null;
    }

    public int getOptionsCount() {
        return hasOptions()?options.length:0;
    }

    public String getOptionAt(int index) {
        return hasOptions()?options[index]:null;
    }

    public String getOrder() {
        return order;
    }

    public String[] getOptions() {
        return options;
    }

}
