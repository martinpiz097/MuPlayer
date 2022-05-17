package org.muplayer.net;

import org.muplayer.console.ConsoleInterpreter;
import org.muplayer.audio.player.Player;

public class DaemonService extends Thread {
    private final Player player;
    private final ConsoleInterpreter consoleInterpreter;

    public DaemonService(Player player) {
        this.player = player;
        this.consoleInterpreter = new ConsoleInterpreter(player);
    }
}
