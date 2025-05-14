package cl.estencia.labs.muplayer.system;

import cl.estencia.labs.muplayer.config.model.MuPlayerInfoKeys;
import cl.estencia.labs.muplayer.config.reader.MuPlayerInfoReader;

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
    public static final String USERNAME = System.getProperty("user.name");

    public static String readVersion() {
        return MuPlayerInfoReader.getInstance().getProperty(MuPlayerInfoKeys.MU_PLAYER_VERSION);
    }
}
