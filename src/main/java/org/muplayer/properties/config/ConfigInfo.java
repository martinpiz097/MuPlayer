package org.muplayer.properties.config;

import lombok.Getter;
import org.muplayer.properties.FilePropertiesSource;
import org.muplayer.properties.PropertiesFiles;
import org.muplayer.properties.PropertiesInfo;

import java.io.File;

public class ConfigInfo extends PropertiesInfo<File> {
    @Getter
    private static ConfigInfo instance = new ConfigInfo();

    protected ConfigInfo() {
        super(new FilePropertiesSource(PropertiesFiles.CONFIG_FILE_PATH));
    }

    @Override
    protected void loadDefaultData() {

    }
}

