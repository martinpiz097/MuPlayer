package org.muplayer.properties;

import java.io.File;
import java.io.InputStream;

public class PropertiesFiles {
    public static final String CONFIG_FILE_PATH = getJarParentPath()+"/config.properties";

    public static final String AUDIO_SUPPORT_RES_PATH = "/audio-support.properties";
    public static final String AUDIO_SUPPORT_FILE_PATH = getFilePath(AUDIO_SUPPORT_RES_PATH);

    public static final String HELP_RES_PATH = "/help.properties";
    public static final String HELP_FILE_PATH = getFilePath(HELP_RES_PATH);

    public static final String MUPLAYER_INFO_RES_PATH = "/muplayer-info.properties";

    public static final String MESSAGES_RES_PATH = "/messages.properties";
    public static final String MESSAGES_FILE_PATH = getFilePath(MESSAGES_RES_PATH);

    public static final String CONSOLE_CODES_RES_PATH = "/console-codes.properties";

    private static String getFilePath(String fileName) {
        return getJarParentPath() + fileName;
    }

    public static String getJarParentPath() {
        final String parentPath = new File(PropertiesFiles.class.getProtectionDomain()
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

    public static InputStream getResStream(String path) {
        return PropertiesFiles.class.getResourceAsStream(path);
    }
}
