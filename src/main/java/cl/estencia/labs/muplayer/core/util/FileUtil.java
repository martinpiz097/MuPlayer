package cl.estencia.labs.muplayer.core.util;

import java.io.File;
import java.io.IOException;

public class FileUtil {

    private FileUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final String FORMAT_NAME_DELIMITER = "\\.";
    private static final String EMPTY = "";

    public static String getFileFormatName(File file) {
        return file != null ? getFileFormatName(file.getName()) : null;
    }

    public static String getFileFormatName(String fileName) {
        final String[] split = fileName.trim().split(FORMAT_NAME_DELIMITER);
        return split.length > 0 ? split[split.length-1] : EMPTY;
    }

    public static String getPath(File file) {
        try {
            return file != null ? file.getCanonicalPath() : null;
        } catch (IOException e) {
            return null;
        }
    }
}
