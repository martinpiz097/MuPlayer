package cl.estencia.labs.muplayer.data.reader;

import lombok.Getter;
import cl.estencia.labs.muplayer.data.reader.base.properties.source.FilePropertiesSource;
import cl.estencia.labs.muplayer.data.ResourceFiles;
import cl.estencia.labs.muplayer.data.reader.base.properties.PropertiesInfo;

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

