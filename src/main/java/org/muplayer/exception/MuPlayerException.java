package org.muplayer.exception;

public class MuPlayerException extends RuntimeException {
    public MuPlayerException(String message) {
        super("Error: "+message);
    }
}
