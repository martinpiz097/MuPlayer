package org.muplayer.net;

import org.muplayer.console.ConsoleExecution;
import org.muplayer.console.ConsoleInterpreter;
import org.orangelogger.sys.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPClient extends Client {
    private final ConsoleInterpreter consoleInterpreter;
    private final Socket clientSocket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public TCPClient(ConsoleInterpreter consoleInterpreter, Socket clientSocket) throws IOException {
        this(consoleInterpreter, clientSocket, clientSocket.getInputStream(), clientSocket.getOutputStream());
    }

    public TCPClient(ConsoleInterpreter consoleInterpreter, Socket clientSocket, InputStream inputStream, OutputStream outputStream) {
        this.consoleInterpreter = consoleInterpreter;
        this.clientSocket = clientSocket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        start();
    }

    @Override
    public void close() throws IOException {
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
        Logger.getLogger(this, getLoggerHeader()+"Client connected.").info();
        while (true) {
            try {
                command = recvString();
                if (command != null && !command.trim().isEmpty()) {
                    Logger.getLogger(this, getLoggerHeader()+"Command received: "+command).info();
                    consoleExecution = consoleInterpreter.executeCommand(command.toLowerCase());
                    Logger.getLogger(this, getLoggerHeader()+"Command executed. ").info();
                    if (consoleExecution != null && consoleExecution.hasOutput()) {
                        Logger.getLogger(this, getLoggerHeader()+"Waiting for command processing...").info();
                        sendString(consoleExecution.getOutput().toString());
                        Logger.getLogger(this, getLoggerHeader()+"Command response sent: "+consoleExecution.getOutput().toString()).info();
                    }
                }
                Thread.sleep(1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
