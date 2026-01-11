package cl.estencia.labs.muplayer.io.net;

import cl.estencia.labs.muplayer.config.model.MuPlayerConfigKeys;
import cl.estencia.labs.muplayer.config.reader.MuPlayerConfigReader;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;

@Getter
@Setter
public class NetworkServer {
    private final ServerSocket serverSocket;
    private final List<Client> listClients;

    public NetworkServer() throws IOException {
        this.serverSocket = new ServerSocket(getServerSetupPort());
        this.serverSocket.setSoTimeout(getSetSoTimeout());
        this.listClients = CollectionUtil.newLinkedList();
    }

    private int getServerSetupPort() {
        String portProperty = MuPlayerConfigReader.getInstance().getProperty(
                MuPlayerConfigKeys.DAEMON_SERVER_PORT);
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
