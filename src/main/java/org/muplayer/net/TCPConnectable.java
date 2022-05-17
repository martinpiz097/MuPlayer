package org.muplayer.net;

import java.io.*;
import java.net.Socket;

public class TCPConnectable implements Connectable {
    private final Socket clientSocket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    public TCPConnectable(Socket clientSocket) throws IOException {
        this(clientSocket, clientSocket.getInputStream(), clientSocket.getOutputStream());
    }

    public TCPConnectable(Socket clientSocket, InputStream inputStream, OutputStream outputStream) {
        this.clientSocket = clientSocket;
        this.inputStream = new DataInputStream(inputStream);
        this.outputStream = new DataOutputStream(outputStream);
    }

    @Override
    public void close() throws IOException {
        clientSocket.close();
    }

    @Override
    public void sendString(String str) throws IOException {
        outputStream.writeUTF(str);
    }

    @Override
    public String recvString() throws IOException {
        return inputStream.readUTF();
    }
}
