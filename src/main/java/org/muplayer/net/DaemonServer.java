package org.muplayer.net;

import lombok.Data;
import org.muplayer.properties.MuPlayerInfo;
import org.muplayer.properties.MuPlayerInfoKeys;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Data
public class DaemonServer {
    private final ServerSocket serverSocket;
    private final List<Client> listClients;

    public DaemonServer() throws IOException {
        serverSocket = new ServerSocket(Integer.parseInt(MuPlayerInfo.getInstance().getProperty(
                MuPlayerInfoKeys.DAEMON_SERVER_PORT)));
        this.listClients = new ArrayList<>();
    }

    public Socket getRequestSocket() throws IOException {
        return serverSocket.accept();
    }

    public void addClient(Client client) {
        listClients.add(client);
    }
}
