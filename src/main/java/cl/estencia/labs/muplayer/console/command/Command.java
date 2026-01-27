package cl.estencia.labs.muplayer.console.command;

import cl.estencia.labs.muplayer.core.util.CollectionUtil;

import java.util.Arrays;
import java.util.List;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.SPACE_CHAR;

public class Command {
    private final String order;
    private final String[] options;

    private static final String CMD_SPLIT_DELIMITER = String.valueOf(SPACE_CHAR);
    private static final char CMD_SPLIT_DELIMITER_CHAR = SPACE_CHAR;
    private static final byte ORDER_INDEX = 0;
    private static final byte FIRST_OPTION_INDEX = 1;
    private static final byte NO_OPTIONS_SPLIT_VALUE = 1;
    private static final byte NO_OPTIONS_VALUE = 0;

    public Command(String order, String... options) {
        this.order = order;
        this.options = options != null ? options : new String[0];
    }

    public Command(String strCmd) {
        if (strCmd == null || strCmd.isBlank()) {
            order = "";
            options = new String[0];
        } else {
            String[] cmdSplit = strCmd.split(CMD_SPLIT_DELIMITER);
            if (cmdSplit.length > 0) {
                order = cmdSplit[ORDER_INDEX];
                if (cmdSplit.length > NO_OPTIONS_SPLIT_VALUE) {
                    options = Arrays.copyOfRange(cmdSplit, FIRST_OPTION_INDEX, cmdSplit.length);
                } else {
                    options = new String[0];
                }
            } else {
                order = "";
                options = new String[0];
            }
        }
    }

    public boolean hasNotOptions() {
        return !hasOptions();
    }

    public boolean hasOptions() {
        return options.length > 0;
    }

    public int getOptionsCount() {
        return hasOptions() ? options.length : NO_OPTIONS_VALUE;
    }

    public String getOptionAt(int index) {
        return hasOptions() && getOptionsCount() > index
                ? options[index]
                : null;
    }

    public Number getOptionAsNumber(int index) {
        try {
            final String option = getOptionAt(index);

            return option != null ? Integer.parseInt(option.trim()) : null;
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

    public List<String> getOptionsAsList() {
        return Arrays.asList(options);
    }

    public String getOptionsAsString() {
        if (!hasOptions()) {
            return null;
        }

        StringBuilder sbOptions = new StringBuilder();
        int optionsCount = getOptionsCount();
        for (int i = 0; i < optionsCount; i++) {
            sbOptions.append(options[i]).append(SPACE_CHAR);
        }

        sbOptions.deleteCharAt(sbOptions.length() - 1);
        return sbOptions.toString();
    }

    public List<String> getAllAsList() {
        List<String> listCmd = CollectionUtil.newMiniList();
        listCmd.add(order.trim());

        int optCount = getOptionsCount();
        for (int i = 0; i < optCount; i++) {
           listCmd.add(options[i].trim());
        }

        return listCmd;
    }

    @Override
    public String toString() {
        if (!hasOptions()) {
            return order;
        }

        StringBuilder sbCmd = new StringBuilder(order);
        int optionsCount = getOptionsCount();
        for (int i = 0; i < optionsCount; i++) {
            sbCmd.append(CMD_SPLIT_DELIMITER_CHAR).append(options[i]);
        }

        return sbCmd.toString();
    }
}
