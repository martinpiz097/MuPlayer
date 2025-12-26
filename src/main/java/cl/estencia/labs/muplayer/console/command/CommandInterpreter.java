package cl.estencia.labs.muplayer.console.command;

import cl.estencia.labs.muplayer.console.model.ConsoleOutput;

public interface CommandInterpreter {
    ConsoleOutput executeCommand(Command cmd) throws Exception;
    default ConsoleOutput executeCommand(String strCmd) throws Exception {
        if (strCmd != null && !strCmd.isEmpty())
            return executeCommand(new Command(strCmd));
        else
            return null;
    }
}
