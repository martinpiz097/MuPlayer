package org.muplayer.main;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public interface CommandInterpreter {
    public void interprate(Command cmd) throws IOException, UnsupportedAudioFileException, LineUnavailableException;
    public default void interprate(String strCmd) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if (strCmd != null && !strCmd.isEmpty())
            interprate(new Command(strCmd));
    }
}
