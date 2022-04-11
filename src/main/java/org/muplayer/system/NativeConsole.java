package org.muplayer.system;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingInputStream;
import org.jline.utils.NonBlockingReader;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class NativeConsole {
    private final Terminal terminal;
    private final NonBlockingReader reader;
    private final PrintWriter writer;

    public NativeConsole() throws IOException {
        terminal = TerminalBuilder.builder()
                .exec(true)
                .system(true)
                .jna(true)
                .nativeSignals(true)
                .dumb(true)
                .jansi(true)
                .paused(false)
                .name("MuPlayer")
                .build();
        terminal.enterRawMode();
        reader = terminal.reader();
        writer = terminal.writer();

        //new NonBlockingInputStream()
    }

    public char readChar() throws IOException {
        return (char) reader.read();
    }

    public int readInt() throws IOException {
        return reader.read();
    }

    public String readLine() throws IOException {
        final char[] buffer = new char[100];
        return new String(Arrays.copyOf(buffer, reader.readBuffered(buffer)));
    }

    public void write(char c) {
        writer.write(c);
    }

    public void write(int i) {
        writer.write(i);
    }

    public void write(String str) {
        writer.write(str);
    }

    public Terminal getTerminal() {
        return terminal;
    }
}
