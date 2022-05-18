package org.muplayer.system;

import org.muplayer.properties.MuPlayerInfo;
import org.muplayer.properties.ConfigInfoKeys;

public class SysInfo {
    public static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    public static final boolean IS_WINDOWS = OS_NAME.contains("windows");
    public static final boolean IS_LINUX = OS_NAME.contains("linux");
    public static final boolean IS_MAC = (!IS_WINDOWS && !IS_LINUX) &&
            (OS_NAME.contains("mac") || OS_NAME.contains("osx"));

    public static final boolean IS_UNIX = IS_LINUX || IS_MAC;
    public static final String USERNAME = System.getProperty("user.name");

    public static String readVersion() {
        return MuPlayerInfo.getInstance().getProperty(ConfigInfoKeys.APP_VERSION);
    }
}
