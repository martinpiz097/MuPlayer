package org.muplayer.system;

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
        return getDataFromStream(DataUtil.class.getResourceAsStream(path));
    }

}
