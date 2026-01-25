package cl.estencia.labs.muplayer.io.net;

import cl.estencia.labs.muplayer.console.PlayerCommandInterpreter;
import cl.estencia.labs.muplayer.console.model.ConsoleOutput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static cl.estencia.labs.muplayer.core.log.ConsolePrinter.infoLine;

public class TCPClient extends Client {
    private final Socket clientSocket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public TCPClient(PlayerCommandInterpreter playerCommandInterpreter, Socket clientSocket) throws IOException {
        this(playerCommandInterpreter, clientSocket, clientSocket.getInputStream(), clientSocket.getOutputStream());
    }

    public TCPClient(PlayerCommandInterpreter playerCommandInterpreter, Socket clientSocket, InputStream inputStream, OutputStream outputStream) {
        super(playerCommandInterpreter);
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
        ConsoleOutput consoleOutput;
        infoLine("Client connected from IP " + clientSocket.getRemoteSocketAddress().toString());
        while (true) {
            try {
                command = recvString();
                if (command != null && !command.trim().isEmpty()) {
                    command = command.trim();
                    infoLine(getLoggerHeader()+"Command received: "+command);

                    consoleOutput = playerCommandInterpreter.execute(command.toLowerCase());
                    infoLine(getLoggerHeader()+"Command executed. ");

                    if (consoleOutput != null && consoleOutput.hasOutput()) {
                        infoLine(getLoggerHeader()+"Waiting for command processing...");
                        sendString(consoleOutput.getOutputMsg());
                        infoLine(getLoggerHeader()+"Command response sent: "+ consoleOutput.getOutputMsg());
                    }
                }
                sleep(1);
            } catch (Exception e) {
                if (e.getMessage().equalsIgnoreCase("Socket closed")) {
                    break;
                }
                else {
                    e.printStackTrace();
                }
            }
        }

        infoLine("Client with IP "+clientSocket.getRemoteSocketAddress().toString() + " closed");
    }
}
