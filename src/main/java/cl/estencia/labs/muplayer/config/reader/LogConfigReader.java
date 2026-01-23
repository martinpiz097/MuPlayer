package cl.estencia.labs.muplayer.config.reader;

import cl.estencia.labs.muplayer.config.Resources;
import cl.estencia.labs.muplayer.config.base.properties.PropertiesInfo;
import cl.estencia.labs.muplayer.config.base.properties.source.StreamPropertiesSource;
import lombok.Getter;

import java.io.InputStream;

public class LogConfigReader extends PropertiesInfo<InputStream> {

    @Getter
    private static LogConfigReader instance = new LogConfigReader();

    protected LogConfigReader() {
        super(new StreamPropertiesSource(Resources.LOG_CONFIG_RES_PATH));
    }

    @Override
    protected void loadDefaultData() {

    }
}
