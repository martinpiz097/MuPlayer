package org.muplayer.system;

import org.muplayer.properties.ConfigInfo;

import java.io.IOException;
import java.io.InputStream;

import static org.muplayer.properties.ConfigInfo.INFO_FILE_NAKE;

public class SysInfo {
    public static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    public static final boolean IS_WINDOWS = OS_NAME.contains("windows");
    public static final boolean IS_LINUX = OS_NAME.contains("linux");
    public static final boolean IS_MAC = (!IS_WINDOWS && !IS_LINUX) &&
            (OS_NAME.contains("mac") || OS_NAME.contains("osx"));

    public static final boolean IS_UNIX = IS_LINUX || IS_MAC;
    public static final String USERNAME = System.getProperty("user.name");
    public static final String VERSION;

    static {
        VERSION = readVersion();
    }

    private static String readVersion() {
        try (InputStream inputStream = ConfigInfo.class.getResourceAsStream(INFO_FILE_NAKE)) {
            final StringBuilder sbVersion = new StringBuilder();
            int read;
            if (inputStream != null) {
                while ((read = inputStream.read()) != -1) {
                    sbVersion.append((char) read);
                }
            }
            return sbVersion.toString();
        } catch (IOException e) {
            return null;
        }
    }



}
