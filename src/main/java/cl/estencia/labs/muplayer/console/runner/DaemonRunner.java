package cl.estencia.labs.muplayer.console.runner;

import cl.estencia.labs.muplayer.audio.player.MuPlayer;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.data.CacheVar;
import cl.estencia.labs.muplayer.net.DaemonServer;
import cl.estencia.labs.muplayer.net.TCPClient;
import org.orangelogger.sys.Logger;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class DaemonRunner extends ConsoleRunner {
    private final DaemonServer daemonServer;

    public DaemonRunner() throws IOException {
        this((File) null);
    }

    public DaemonRunner(String folder) throws IOException {
        this(new File(folder));
    }

    public DaemonRunner(File rootFolder) throws IOException {
        this(new MuPlayer(rootFolder));
    }

    public DaemonRunner(Player player) throws IOException {
        super(player);
        this.daemonServer = new DaemonServer();
    }

    public void shutdown() throws IOException {
        interpreter.setOn(false);
        daemonServer.shutdownServer();
    }

    @Override
    public void run() {
        Logger.getLogger(this, "MuPlayer daemon mode started.").info();
        interpreter.setOn(true);

        Socket reqSocket = null;

        Logger.getLogger(this, "Waiting clients...").info();
        while (interpreter.isOn() && daemonServer.isAlive()) {
            try {
                reqSocket = daemonServer.getRequestSocket();
                if (reqSocket != null) {
                    daemonServer.addClient(new TCPClient(super.interpreter, reqSocket));
                    Logger.getLogger(this, "Client connected from IP "+reqSocket.getRemoteSocketAddress()
                            .toString()).info();
                    Logger.getLogger(this, "Waiting clients...").info();
                }
                Thread.sleep(100);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        Logger.getLogger(this, "Daemon server closed!");
        try {
            daemonServer.shutdownServer();
            final ConsoleRunner runner = globalCacheManager.loadValue(CacheVar.RUNNER);
            if (runner == null || runner instanceof DaemonRunner)
                System.exit(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
