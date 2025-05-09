package cl.estencia.labs.muplayer.muplayer.data.properties.config;

import lombok.Getter;
import cl.estencia.labs.muplayer.muplayer.data.properties.FilePropertiesSource;
import cl.estencia.labs.muplayer.muplayer.data.properties.ResourceFiles;
import cl.estencia.labs.muplayer.muplayer.data.properties.PropertiesInfo;

import java.io.File;

public class ConfigInfo extends PropertiesInfo<File> {
    @Getter
    private static ConfigInfo instance = new ConfigInfo();

    protected ConfigInfo() {
        super(new FilePropertiesSource(ResourceFiles.CONFIG_FILE_PATH));
    }

    @Override
    protected void loadDefaultData() {

    }
}

