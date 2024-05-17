package org.muplayer.net;

import lombok.Data;
import org.muplayer.properties.info.MuPlayerInfo;
import org.muplayer.properties.info.MuPlayerInfoKeys;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

@Data
public class DaemonServer {
    private final ServerSocket serverSocket;
    private final List<Client> listClients;

    public DaemonServer() throws IOException {
        serverSocket = new ServerSocket(Integer.parseInt(MuPlayerInfo.getInstance().getProperty(
                MuPlayerInfoKeys.DAEMON_SERVER_PORT)));
        serverSocket.setSoTimeout(3000);
        this.listClients = CollectionUtil.newFastList();
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
            else
                throw e;
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
