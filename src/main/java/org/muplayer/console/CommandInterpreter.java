package org.muplayer.console;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public interface CommandInterpreter {
    void interprate(Command cmd) throws Exception;
    default void interprate(String strCmd) throws Exception {
        if (strCmd != null && !strCmd.isEmpty())
            interprate(new Command(strCmd));
    }
}
