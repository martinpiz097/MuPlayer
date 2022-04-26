package org.muplayer.properties;

import org.muplayer.system.SysInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ConfigInfo {
    public static final String CONFIG_FILE_NAME = "config.properties";
    public static final String AUDIO_SUPPORT_FILE_NAME = "audio-support.properties";
    public static final String HELP_FILE_NAME = "help.properties";
    public static final String INFO_FILE_NAKE = "muplayer-info.properties";

    private static String readConfigPath() {
        File jarPath = new File(ConfigInfo.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        File configFile = new File(jarPath.getParent(), CONFIG_FILE_NAME);
        try {
            return configFile.exists() ? configFile.getCanonicalPath() : "";
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
