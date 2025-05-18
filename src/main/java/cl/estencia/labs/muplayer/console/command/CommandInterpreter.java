package cl.estencia.labs.muplayer.console.command;

import cl.estencia.labs.muplayer.console.exception.ConsoleExecution;

public interface CommandInterpreter {
    ConsoleExecution executeCommand(Command cmd) throws Exception;
    default ConsoleExecution executeCommand(String strCmd) throws Exception {
        if (strCmd != null && !strCmd.isEmpty())
            return executeCommand(new Command(strCmd));
        else
            return null;
    }
}
