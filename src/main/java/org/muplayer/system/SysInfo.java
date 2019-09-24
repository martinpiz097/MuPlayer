package org.muplayer.system;

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

    static {
        VERSION = readVersion();
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

}
