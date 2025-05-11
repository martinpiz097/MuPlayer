package cl.estencia.labs.muplayer.config.reader;

import lombok.Getter;
import cl.estencia.labs.muplayer.config.base.properties.source.FilePropertiesSource;
import cl.estencia.labs.muplayer.config.ResourceFiles;
import cl.estencia.labs.muplayer.config.base.properties.PropertiesInfo;

import java.io.File;

public class ConfigReader extends PropertiesInfo<File> {
    @Getter
    private static ConfigReader instance = new ConfigReader();

    protected ConfigReader() {
        super(new FilePropertiesSource(ResourceFiles.CONFIG_FILE_PATH));
    }

    @Override
    protected void loadDefaultData() {

    }
}

