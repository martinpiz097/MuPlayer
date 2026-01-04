package cl.estencia.labs.muplayer.io.net;

import java.io.IOException;

public interface Connectable {
    void close() throws IOException;
    void sendString(String str) throws IOException;
    String recvString() throws IOException;
}
