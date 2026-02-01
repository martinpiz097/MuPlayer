package cl.estencia.labs.muplayer.audio.util;

import cl.estencia.labs.muplayer.audio.track.data.Cover;

import java.io.File;

public class CoverUtil {
    // TODO: leer tmp segun sistema operativo
    private static final String FILE_URI_START = "file://";
    private static final String TMP_DIR = "/tmp";

    public static String createTempUri(Cover cover) {
        File coverFile = cover.saveInLocalStorage(TMP_DIR, String.valueOf(System.nanoTime()));
        return coverFile != null ? FILE_URI_START + coverFile.getPath() : null;
    }

}
