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
    public static final String CMD_CODES_FILE_RES_PATH = "/console-code.properties";

    public static String getJarParentPath() {
        final String parentPath = new File(PropertiesFilesInfo.class.getProtectionDomain()
                .getCodeSource().getLocation().getFile()).getParent();
        final String jarParentPath = parentPath.endsWith("/") ? parentPath.substring(0, parentPath.length() - 1) : parentPath;
        System.out.println("TEST: " + jarParentPath);

        if (jarParentPath.contains("!")) {
            String path = jarParentPath.split("!")[0];
            path = path.substring(0, path.lastIndexOf("/"));
            System.out.println("TEST 2: "+path);
            return path;
        }
        else
            return jarParentPath;

    }
}
