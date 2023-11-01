package org.muplayer.properties;

public class ConfigInfo extends PropertiesInfo {
    private static ConfigInfo singleton = new ConfigInfo();

    public static ConfigInfo getInstance() {
        return singleton;
    }

    protected ConfigInfo() {
        super(PropertiesFiles.CONFIG_FILE_PATH);
    }

}

