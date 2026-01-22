package cl.estencia.labs.muplayer.console.common.enums;

// cuando un key interceptor esta en modo global
// puede ser usado tanto en modo de consola COMMAND y SINGLE_SHORCUTS
// sino, solo es valido en el modo de consola COMMAND, para no interrumpir
// a los shorcuts configurados
public enum InterceptorMode {
    GLOBAL, SIMPLE
}
