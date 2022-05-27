package org.muplayer.net;

import org.muplayer.audio.player.Player;
import org.muplayer.console.ConsoleInterpreter;
import org.muplayer.properties.MuPlayerInfo;
import org.orangelogger.sys.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class DaemonRunner extends Thread {
    private final ConsoleInterpreter consoleInterpreter;
    private final DaemonServer daemonServer;

    public DaemonRunner(Player player) throws IOException {
        this.consoleInterpreter = new ConsoleInterpreter(player);
        this.daemonServer = new DaemonServer();
    }

    @Override
    public void run() {
        Logger.getLogger(this, "MuPlayer daemon mode started.").info();
        while (true) {
            try {
                Logger.getLogger(this, "Waiting clients...").info();
                daemonServer.addClient(new TCPClient(consoleInterpreter, daemonServer.getSocketRequest()));
                Thread.sleep(1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
