package cl.estencia.labs.muplayer.config.reader;

import lombok.Getter;
import cl.estencia.labs.muplayer.config.ResourceFiles;
import cl.estencia.labs.muplayer.config.base.properties.PropertiesInfo;
import cl.estencia.labs.muplayer.config.base.properties.source.StreamPropertiesSource;

import java.io.InputStream;

public class LogConfigReader extends PropertiesInfo<InputStream> {

    @Getter
    private static LogConfigReader instance = new LogConfigReader();

    protected LogConfigReader() {
        super(new StreamPropertiesSource(ResourceFiles.LOG_CONFIG_RES_PATH));
    }

    @Override
    protected void loadDefaultData() {

    }
}
