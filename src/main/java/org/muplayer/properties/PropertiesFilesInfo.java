package org.muplayer.properties;

import java.io.File;

public class PropertiesFilesInfo {
    public static final String CONFIG_FILE_PATH = getJarParentPath()+"/config.properties";
    public static final String AUDIO_SUPPORT_FILE_RES_PATH = "/audio-support.properties";
    public static final String AUDIO_SUPPORT_FILE_NAME = getJarParentPath()+"/audio-support.properties";
    public static final String HELP_FILE_PATH = getJarParentPath()+"/help.properties";
    public static final String HELP_FILE_RES_PATH = "/help.properties";
    public static final String MUPLAYER_INFO_FILE_RES_PATH = "/muplayer-info.properties";
    public static final String MESSAGES_FILE_RES_PATH = "/messages.properties";

    public static String getJarParentPath() {
        final String parentPath = new File(PropertiesFilesInfo.class.getProtectionDomain()
                .getCodeSource().getLocation().getFile()).getParent();
        return parentPath.endsWith("/") ? parentPath.substring(0, parentPath.length()-1) : parentPath;
    }
}
