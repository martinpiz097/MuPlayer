package org.muplayer.main;

import java.io.FileNotFoundException;

public interface CommandInterpreter {
    public void interprate(Command cmd) throws FileNotFoundException;
}
