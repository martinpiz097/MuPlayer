package org.muplayer.net;

import java.io.IOException;

public interface Connectable {
    void close() throws IOException;
    void sendString(String str) throws IOException;
    String recvString() throws IOException;
}
