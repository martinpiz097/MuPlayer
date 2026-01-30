package cl.estencia.labs.muplayer.console.unixconsole.listener;

public interface Interceptor<E> {
    void intercept(E event);
}
