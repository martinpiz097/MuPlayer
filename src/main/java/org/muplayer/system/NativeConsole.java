package org.muplayer.system;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import java.io.IOException;

public class NativeConsole {
    private final Terminal terminal;
    private final NonBlockingReader reader;

    public NativeConsole() throws IOException {
        terminal = TerminalBuilder.builder()
                .jna(true)
                .system(true)
                .build();
        terminal.enterRawMode();
        reader = terminal.reader();
    }

    public char readChar() throws IOException {
        return (char) reader.read();
    }

    public Terminal getTerminal() {
        return terminal;
    }


    public NonBlockingReader getReader() {
        return reader;
    }

}
