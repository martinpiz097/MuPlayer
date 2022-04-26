package org.muplayer.util;

import java.io.File;
import java.io.IOException;

public class FileUtil {
    public static String getFormatName(String fileName) {
        final String[] split = fileName.trim().split("\\.");
        return split.length > 0
                ? split[split.length-1]
                : "";
    }

    public static String getPath(File file) {
        try {
            return file != null ? file.getCanonicalPath() : null;
        } catch (IOException e) {
            return null;
        }
    }
}
