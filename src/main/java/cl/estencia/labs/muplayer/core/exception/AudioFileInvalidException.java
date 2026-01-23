package cl.estencia.labs.muplayer.core.exception;

import java.io.File;

public class AudioFileInvalidException extends MuPlayerException {
    public AudioFileInvalidException(File audioFile) {
        super(audioFile != null
                ? (audioFile.isDirectory()
                    ? audioFile.getName() + " file is a directory" : audioFile.getName() + " not exists!")
                : "Audio file is null");
    }
}
