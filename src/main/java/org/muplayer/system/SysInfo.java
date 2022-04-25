package org.muplayer.system;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SysInfo {
    public static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    public static final boolean IS_WINDOWS = OS_NAME.contains("windows");
    public static final boolean IS_LINUX = OS_NAME.contains("linux");
    public static final boolean IS_MAC = (!IS_WINDOWS && !IS_LINUX) &&
            (OS_NAME.contains("mac") || OS_NAME.contains("osx"));

    public static final boolean IS_UNIX = IS_LINUX || IS_MAC;
    public static final String USERNAME = System.getProperty("user.name");
    public static final String VERSION;
    public static final String CONFIG_FILE_NAME = "config.properties";
    public static String CONFIG_FILE_PATH;

    public static final String AUDIO_SUPPORT_FILE_NAME = "audio-support.properties";



    static {
        VERSION = readVersion();
        CONFIG_FILE_PATH = readConfigPath();
    }

    private static String readVersion() {
        try {
            InputStream inputStream = SysInfo.class.getResource("/version.txt").openStream();
            StringBuilder sbVersion = new StringBuilder();

            int read;
            while ((read = inputStream.read()) != -1) {
                sbVersion.append((char)read);
            }
            return sbVersion.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private static String readConfigPath() {
        File jarPath = new File(SysInfo.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        File configFile = new File(jarPath.getParent(), CONFIG_FILE_NAME);
        try {
            if (configFile.exists()) {
                return configFile.getCanonicalPath();
            }
            else {
                return CONFIG_FILE_NAME;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
