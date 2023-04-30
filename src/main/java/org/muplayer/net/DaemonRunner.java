package org.muplayer.net;

import org.muplayer.audio.player.MusicPlayer;
import org.muplayer.audio.player.Player;
import org.muplayer.console.ConsoleRunner;
import org.orangelogger.sys.Logger;

import java.io.File;
import java.io.IOException;

public class DaemonRunner extends ConsoleRunner {
    private final DaemonServer daemonServer;

    public DaemonRunner() throws IOException {
        this((File) null);
    }

    public DaemonRunner(String folder) throws IOException {
        this(new File(folder));
    }

    public DaemonRunner(File rootFolder) throws IOException {
        this(new MusicPlayer(rootFolder));
    }

    public DaemonRunner(Player player) throws IOException {
        super(player);
        this.daemonServer = new DaemonServer();
    }

    @Override
    public void run() {
        Logger.getLogger(this, "MuPlayer daemon mode started.").info();
        while (true) {
            try {
                Logger.getLogger(this, "Waiting clients...").info();
                daemonServer.addClient(new TCPClient(super.interpreter, daemonServer.getRequestSocket()));
                Thread.sleep(1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
