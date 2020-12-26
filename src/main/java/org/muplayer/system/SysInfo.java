package org.muplayer.system;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SysInfo {
    public static final String OSNAME = System.getProperty("os.name").toLowerCase();
    public static final boolean ISWINDOWS = OSNAME.contains("windows");
    public static final boolean ISLINUX = OSNAME.contains("linux");
    public static final boolean ISMAC = (!ISWINDOWS && !ISLINUX) &&
            (OSNAME.contains("mac") || OSNAME.contains("osx"));

    public static final boolean ISUNIX = ISLINUX || ISMAC;
    public static final String USERNAME = System.getProperty("user.name");
    public static final String VERSION;
    public static final String CONFIG_FILE_NAME = "config.properties";
    public static String CONFIG_FILE_PATH;

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
