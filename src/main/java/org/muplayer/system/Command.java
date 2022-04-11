package org.muplayer.system;

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
        return hasOptions() ?
                ((getOptionsCount() > index)
                        ? options[index] : null) : null;
    }

    public Number getOptionAsNumber(int index) {
        final String option = getOptionAt(index);
        try {
            return option == null ? null : Double.parseDouble(option);
        } catch (NumberFormatException e){
            return null;
        }
    }

    public String getOrder() {
        return order;
    }

    public String[] getOptions() {
        return options;
    }

    public String getOptionsAsString() {
        if (hasOptions()) {
            StringBuilder sbOptions = new StringBuilder();
            for (int i = 0; i < options.length; i++)
                sbOptions.append(options[i]).append(' ');
            sbOptions.deleteCharAt(sbOptions.length()-1);
            return sbOptions.toString();
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sbCmd = new StringBuilder();
        sbCmd.append(order);
        if (hasOptions())
            for (int i = 0; i < options.length; i++)
                sbCmd.append(' ').append(options[i]);
        return sbCmd.toString();
    }
}
