package cl.estencia.labs.muplayer.core.exception;

public class MuPlayerRuntimeException extends RuntimeException {
    public MuPlayerRuntimeException(String message) {
        super(message);
    }

    public MuPlayerRuntimeException(Throwable cause) {
        super(cause);
    }
}
