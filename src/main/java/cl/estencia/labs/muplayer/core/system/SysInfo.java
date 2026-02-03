package cl.estencia.labs.muplayer.core.system;

import cl.estencia.labs.muplayer.config.model.MuPlayerConfigKeys;

import static cl.estencia.labs.muplayer.config.reader.ResourceReaders.MUPLAYER_CONFIG_READER;

public class SysInfo {

    private SysInfo() {
        throw new IllegalStateException("Utility class");
    }

    public static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    public static final boolean IS_WINDOWS = OS_NAME.contains("windows");
    public static final boolean IS_LINUX = OS_NAME.contains("linux");
    public static final boolean IS_MAC = (!IS_WINDOWS && !IS_LINUX) &&
            (OS_NAME.contains("mac") || OS_NAME.contains("osx"));

    public static final boolean IS_UNIX = IS_LINUX || IS_MAC;
    public static final String USER_NAME = System.getProperty("user.name");

    public static String readAppVersion() {
        return MUPLAYER_CONFIG_READER.getProperty(MuPlayerConfigKeys.MU_PLAYER_VERSION);
    }

}
