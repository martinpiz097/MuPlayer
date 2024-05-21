package org.muplayer.console;

import java.util.Arrays;

public class Command {
    private final String order;
    private final String[] options;

    private static final String CMD_SPLIT_DELIMITER = " ";
    private static final char CMD_SPLIT_DELIMITER_CHAR = ' ';
    private static final byte ORDER_INDEX = 0;
    private static final byte FIRST_OPTION_INDEX = 1;
    private static final byte UNIQUE_OPTION_CMD_VALUE = 1;
    private static final byte NO_OPTIONS_VALUE = 0;

    public Command(String order, String... options) {
        this.order = order;
        this.options = options;
    }

    public Command(String strCmd) {
        String[] cmdSplit = strCmd.split(CMD_SPLIT_DELIMITER);
        order = cmdSplit[ORDER_INDEX];
        if (cmdSplit.length > UNIQUE_OPTION_CMD_VALUE)
            options = Arrays.copyOfRange(cmdSplit, FIRST_OPTION_INDEX, cmdSplit.length);
        else
            options = null;
    }

    public boolean hasOptions() {
        return options != null;
    }

    public int getOptionsCount() {
        return hasOptions() ? options.length : NO_OPTIONS_VALUE;
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
                sbCmd.append(CMD_SPLIT_DELIMITER_CHAR).append(options[i]);
        return sbCmd.toString();
    }
}
