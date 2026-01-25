package cl.estencia.labs.muplayer.console.runner;

import cl.estencia.labs.muplayer.audio.player.MuPlayer;
import cl.estencia.labs.muplayer.audio.player.MusicPlayer;
import cl.estencia.labs.muplayer.core.cache.CacheVar;
import cl.estencia.labs.muplayer.io.net.NetworkServer;
import cl.estencia.labs.muplayer.io.net.TCPClient;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import static cl.estencia.labs.muplayer.core.cache.CacheVar.PLAYER;
import static cl.estencia.labs.muplayer.core.log.ConsolePrinter.infoLine;

public class DaemonRunner extends ConsoleRunner {
    private final NetworkServer networkServer;

    public DaemonRunner() throws IOException {
        this((File) null);
    }

    public DaemonRunner(String folder) throws IOException {
        this(new File(folder));
    }

    public DaemonRunner(File rootFolder) throws IOException {
        this(new MuPlayer(rootFolder));
    }

    public DaemonRunner(MusicPlayer player) throws IOException {
        super(player);
        this.networkServer = new NetworkServer();
    }

    public void shutdown() throws IOException {
        interpreter.setOn(false);
        networkServer.shutdownServer();
    }

    @Override
    public void run() {
        validateRootFolder();
        globalCacheManager.saveValue(PLAYER, player);

        infoLine("MuPlayer daemon mode started.");
        interpreter.setOn(true);

        Socket reqSocket = null;

        infoLine("Waiting clients...");
        while (interpreter.isOn() && networkServer.isAlive()) {
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
            final ConsoleRunner runner = globalCacheManager.loadValue(CacheVar.RUNNER);
            if (runner == null || runner instanceof DaemonRunner)
                System.exit(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
