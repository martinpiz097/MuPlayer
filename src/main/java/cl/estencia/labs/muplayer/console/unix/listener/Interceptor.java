package cl.estencia.labs.muplayer.console.unix.listener;

public interface Interceptor<E> {
    void intercept(E event);
}
