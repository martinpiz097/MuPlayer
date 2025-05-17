package cl.estencia.labs.muplayer.event;

import java.util.List;

public interface Listenable<L, E> {
    void addListener(L listener);
    List<L> getAllListeners();
    void removeListener(L listener);
    void removeAllListeners();
    void sendEvent(E event);
}
