package org.muplayer.net;

import org.muplayer.console.ConsoleExecution;
import org.muplayer.console.ConsoleInterpreter;
import org.orangelogger.sys.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPClient extends Client {
    private final Socket clientSocket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public TCPClient(ConsoleInterpreter consoleInterpreter, Socket clientSocket) throws IOException {
        this(consoleInterpreter, clientSocket, clientSocket.getInputStream(), clientSocket.getOutputStream());
    }

    public TCPClient(ConsoleInterpreter consoleInterpreter, Socket clientSocket, InputStream inputStream, OutputStream outputStream) {
        super(consoleInterpreter);
        this.clientSocket = clientSocket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        start();
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
        inputStream.close();
        clientSocket.close();
    }

    @Override
    public void sendString(String str) throws IOException {
        outputStream.write(str.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String recvString() throws IOException {
        int read;
        final StringBuilder sbRead = new StringBuilder();
        while (inputStream.available() > 0) {
            read = inputStream.read();
            sbRead.append((char)read);
        }
        return sbRead.toString();
    }

    private String getLoggerHeader() {
        return clientSocket.getInetAddress().getHostAddress()+":"+clientSocket.getPort()+" -> ";
    }

    @Override
    public void run() {
        String command;
        ConsoleExecution consoleExecution;
        Logger.getLogger(this, "Client connected from IP "+clientSocket.getRemoteSocketAddress().toString());
        while (true) {
            try {
                command = recvString();
                if (command != null && !command.trim().isEmpty()) {
                    command = command.trim();
                    Logger.getLogger(this, getLoggerHeader()+"Command received: "+command).info();

                    consoleExecution = consoleInterpreter.executeCommand(command.toLowerCase());
                    Logger.getLogger(this, getLoggerHeader()+"Command executed. ").info();

                    if (consoleExecution != null && consoleExecution.hasOutput()) {
                        Logger.getLogger(this, getLoggerHeader()+"Waiting for command processing...").info();
                        sendString(consoleExecution.getOutputMsg().toString());
                        Logger.getLogger(this, getLoggerHeader()+"Command response sent: "+consoleExecution.getOutputMsg().toString()).info();
                    }
                }
                Thread.sleep(1);
            } catch (Exception e) {
                if (e.getMessage().equalsIgnoreCase("Socket closed"))
                    break;
                else
                    e.printStackTrace();
            }
        }
        Logger.getLogger(this, "Client with IP "+clientSocket.getRemoteSocketAddress().toString() + " closed");
    }
}
