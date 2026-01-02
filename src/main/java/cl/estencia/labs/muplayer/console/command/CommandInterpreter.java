package cl.estencia.labs.muplayer.console.command;

import cl.estencia.labs.muplayer.console.model.ConsoleOutput;

public interface CommandInterpreter {
    ConsoleOutput executeCommand(String strCmd) throws Exception;
    ConsoleOutput executeCommand(Command cmd) throws Exception;
}
