package org.muplayer.net;

import org.muplayer.console.PlayerCommandInterpreter;

public abstract class Client extends Thread implements Connectable {
    protected final PlayerCommandInterpreter playerCommandInterpreter;

    protected Client(PlayerCommandInterpreter playerCommandInterpreter) {
        this.playerCommandInterpreter = playerCommandInterpreter;
    }
}
