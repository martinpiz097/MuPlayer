package org.muplayer.console;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public interface CommandInterpreter {
    ConsoleExecution executeCommand(Command cmd) throws Exception;
    default ConsoleExecution executeCommand(String strCmd) throws Exception {
        if (strCmd != null && !strCmd.isEmpty())
            return executeCommand(new Command(strCmd));
        else
            return null;
    }
}
