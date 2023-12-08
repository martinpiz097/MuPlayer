package org.muplayer.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DataUtil {
    public static String getDataFromStream(InputStream inputStream) throws IOException {
        int r;
        final StringBuilder sbData = new StringBuilder();

        while ((r = inputStream.read()) != -1) {
            sbData.append((char)r);
        }

        return sbData.toString();
    }

    public static String getDataFromResource(String path) throws IOException {
        return getDataFromStream(getResourceAsStream(path));
    }

    public static InputStream getResourceAsStream(String path) {
        return DataUtil.class.getResourceAsStream(path);
    }

    public static BufferedInputStream getResourceAsBuffer(String path) {
        return new BufferedInputStream(getResourceAsStream(path));
    }

}
