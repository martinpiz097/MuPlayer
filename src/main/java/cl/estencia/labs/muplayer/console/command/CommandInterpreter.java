package cl.estencia.labs.muplayer.console.command;

import cl.estencia.labs.muplayer.console.common.enums.ConsoleOrderCode;
import cl.estencia.labs.muplayer.console.model.ConsoleOutput;
import cl.estencia.labs.muplayer.core.exception.MuPlayerException;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;

import java.util.List;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.CMD_DIVISOR;

public interface CommandInterpreter {
    default ConsoleOutput execute(String commandString) throws Exception {
        IO.println("Command to execute: " + commandString);

        if (commandString == null || commandString.isBlank()) {
            return execute(new Command(""));
        }

        if (commandString.contains(CMD_DIVISOR)) {
            final String[] cmdSplit = commandString.split(CMD_DIVISOR);
            final List<String> listExec = CollectionUtil.newLinkedList();
            ConsoleOutput exec;

            for (int i = 0; i < cmdSplit.length; i++) {
                exec = execute(new Command(cmdSplit[i].trim()));
                if (exec != null) {
                    listExec.add(exec.getOutputMsg());
                }
            }

            ConsoleOutput consoleOutput = new ConsoleOutput(commandString);
            consoleOutput.append(listExec.get(listExec.size() - 1), null);
            return consoleOutput;
        } else {
            return execute(new Command(commandString));
        }
    }

    default ConsoleOutput execute(ConsoleOrderCode cmdOrderCode) throws Exception {
        if (cmdOrderCode == null) {
            throw new MuPlayerException("cmdOrderCode is null!");
        }

        return execute(cmdOrderCode.name());
    }


    ConsoleOutput execute(Command cmd) throws Exception;
}
