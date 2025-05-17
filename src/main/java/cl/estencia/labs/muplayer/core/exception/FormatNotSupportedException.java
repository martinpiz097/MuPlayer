package cl.estencia.labs.muplayer.core.exception;

public class FormatNotSupportedException extends MuPlayerException {
    public FormatNotSupportedException(String formatName) {
        super( "Audio format " + formatName + " not supported!");
    }
}
