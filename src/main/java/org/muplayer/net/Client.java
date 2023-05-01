package org.muplayer.net;

import org.muplayer.console.ConsoleInterpreter;

public abstract class Client extends Thread implements Connectable {
    protected final ConsoleInterpreter consoleInterpreter;

    protected Client(ConsoleInterpreter consoleInterpreter) {
        this.consoleInterpreter = consoleInterpreter;
    }
}
