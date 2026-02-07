package cl.estencia.labs.muplayer.console.runner;

import cl.estencia.labs.muplayer.audio.player.MuPlayer;
import cl.estencia.labs.muplayer.audio.player.MusicPlayer;
import cl.estencia.labs.muplayer.core.cache.CacheVar;
import cl.estencia.labs.muplayer.io.net.NetworkServer;
import cl.estencia.labs.muplayer.io.net.TCPClient;
import org.freedesktop.dbus.exceptions.DBusException;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import static cl.estencia.labs.muplayer.core.cache.CacheManager.CACHE;
import static cl.estencia.labs.muplayer.core.cache.CacheVar.PLAYER;
import static cl.estencia.labs.muplayer.core.log.ConsolePrinter.infoLine;

public class DaemonRunner extends ConsoleRunner {
    private final NetworkServer networkServer;

    public DaemonRunner() throws IOException, DBusException {
        this((File) null);
    }

    public DaemonRunner(String folder) throws IOException, DBusException {
        this(new File(folder));
    }

    public DaemonRunner(File rootFolder) throws IOException, DBusException {
        this(new MuPlayer(rootFolder));
    }

    public DaemonRunner(MusicPlayer player) throws IOException {
        super(player);
        this.networkServer = new NetworkServer();
    }

    @Override
    protected boolean canRun() {
        return !Thread.currentThread().isInterrupted()
                && interpreter.isOn() && networkServer.isAlive();
    }

    @Override
    public void shutdown() {
        interpreter.setOn(false);
        try {
            networkServer.shutdownServer();
        } catch (IOException e) {
            log.error("Error on shutdown daemon server: " + e.getMessage());
        }

        interrupt();
    }

    @Override
    public void run() {
        validateRootFolder();
        CACHE.set(PLAYER, player);

        infoLine("MuPlayer daemon mode started.");
        interpreter.setOn(true);

        Socket reqSocket;
        infoLine("Waiting clients...");
        while (canRun()) {
            try {
                reqSocket = networkServer.getRequestSocket();
                if (reqSocket != null) {
                    networkServer.addClient(new TCPClient(super.interpreter, reqSocket));
                    infoLine("Client connected from IP "+reqSocket.getRemoteSocketAddress()
                            .toString());
                    infoLine("Waiting clients...");
                }
                Thread.sleep(100);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        infoLine("Daemon server closed!");
        try {
            networkServer.shutdownServer();
            final ConsoleRunner runner = CACHE.get(CacheVar.RUNNER);
            if (runner == null || runner instanceof DaemonRunner)
                System.exit(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
