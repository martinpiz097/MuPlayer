package cl.estencia.labs.muplayer.core.exception;

public class MuPlayerException extends RuntimeException {
    public MuPlayerException(String message) {
        super("Error: " + message);
    }

    public MuPlayerException(Throwable cause) {
        super(cause);
    }
}
