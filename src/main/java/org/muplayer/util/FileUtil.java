package org.muplayer.util;

import java.io.File;
import java.io.IOException;

public class FileUtil {
    public static String getPath(File file) {
        try {
            return file != null ? file.getCanonicalPath() : null;
        } catch (IOException e) {
            return null;
        }
    }
}
