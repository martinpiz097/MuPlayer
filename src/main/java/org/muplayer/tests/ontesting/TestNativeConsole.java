package org.muplayer.tests.ontesting;

import org.muplayer.system.NativeConsole;

import java.io.IOException;

public class TestNativeConsole {
    public static void main(String[] args) throws IOException {
        NativeConsole nativeConsole = new NativeConsole();
        int read;
        while (true) {
            read = nativeConsole.readInt();
            nativeConsole.write(read);
            if (read == '\n')
                nativeConsole.write("> ");
        }
    }
}
