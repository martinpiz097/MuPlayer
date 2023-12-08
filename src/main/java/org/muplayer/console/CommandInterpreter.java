package org.muplayer.console;

public interface CommandInterpreter {
    ConsoleExecution executeCommand(Command cmd) throws Exception;
    default ConsoleExecution executeCommand(String strCmd) throws Exception {
        if (strCmd != null && !strCmd.isEmpty())
            return executeCommand(new Command(strCmd));
        else
            return null;
    }
}
