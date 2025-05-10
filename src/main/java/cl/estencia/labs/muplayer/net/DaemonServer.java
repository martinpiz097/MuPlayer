package cl.estencia.labs.muplayer.net;

import lombok.Data;
import cl.estencia.labs.muplayer.data.properties.info.MuPlayerInfo;
import cl.estencia.labs.muplayer.data.properties.info.MuPlayerInfoKeys;
import cl.estencia.labs.muplayer.util.CollectionUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;

@Data
public class DaemonServer {
    private final ServerSocket serverSocket;
    private final List<Client> listClients;

    public DaemonServer() throws IOException {
        this.serverSocket = new ServerSocket(getServerSetupPort());
        this.serverSocket.setSoTimeout(getSetSoTimeout());
        this.listClients = CollectionUtil.newLinkedList();
    }

    private int getServerSetupPort() {
        String portProperty = MuPlayerInfo.getInstance().getProperty(
                MuPlayerInfoKeys.DAEMON_SERVER_PORT);
        return portProperty != null ? Integer.parseInt(portProperty) : 0;
    }

    private int getSetSoTimeout() {
        return 3000;
    }

    public boolean isAlive() {
        return !serverSocket.isClosed();
    }

    public Socket getRequestSocket() throws IOException {
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            if (e instanceof SocketTimeoutException
                    || (e instanceof SocketException
                    && e.getMessage().equalsIgnoreCase("Socket closed"))) {
                return null;
            }
            else {
                throw e;
            }
        }
    }

    public void addClient(Client client) {
        listClients.add(client);
    }

    public void shutdownServer() throws IOException {
        if (isAlive()) {
            listClients.parallelStream().forEach(client -> {
                try {
                    client.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            serverSocket.close();
        }
    }

}
