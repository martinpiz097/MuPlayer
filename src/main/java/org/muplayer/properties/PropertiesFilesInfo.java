package org.muplayer.properties;

import java.io.File;

public class PropertiesFilesInfo {
    public static final String CONFIG_FILE_PATH = getJarParentPath()+"/config.properties";
    public static final String AUDIO_SUPPORT_FILE_NAME = "audio-support.properties";
    public static final String HELP_FILE_PATH = "/help.properties";
    public static final String INFO_FILE_PATH = "/muplayer-info.properties";

    private static String getJarParentPath() {
        final String parentPath = new File(PropertiesFilesInfo.class.getProtectionDomain()
                .getCodeSource().getLocation().getFile()).getParent();

        return parentPath.endsWith("/") ? parentPath.substring(0, parentPath.length()-1) : parentPath;
    }
}
