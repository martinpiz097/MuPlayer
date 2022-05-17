package org.muplayer.net;

import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import org.muplayer.console.ConsoleInterpreter;
import org.muplayer.audio.player.Player;
import org.muplayer.properties.MuPlayerInfo;
import org.orangelogger.sys.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class DaemonService extends Thread {
    private final Player player;
    private final ConsoleInterpreter consoleInterpreter;

    private final ServerSocket serverSocket;
    private final List<Client> listClients;

    public DaemonService(Player player) throws IOException {
        this.player = player;
        this.consoleInterpreter = new ConsoleInterpreter(player);
        serverSocket = new ServerSocket(Integer.parseInt(MuPlayerInfo.getInstance().getProperty("daemon.server.port")));
        this.listClients = new ArrayList<>();
    }

    @Override
    public void run() {
        Logger.getLogger(this, "MuPlayer daemon mode started.").info();
        while (true) {
            try {
                Logger.getLogger(this, "Waiting clients...").info();
                listClients.add(new TCPClient(consoleInterpreter, serverSocket.accept()));
                Thread.sleep(1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
