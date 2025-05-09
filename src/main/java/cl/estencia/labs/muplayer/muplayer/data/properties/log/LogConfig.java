package cl.estencia.labs.muplayer.muplayer.data.properties.log;

import lombok.Getter;
import cl.estencia.labs.muplayer.muplayer.data.properties.ResourceFiles;
import cl.estencia.labs.muplayer.muplayer.data.properties.PropertiesInfo;
import cl.estencia.labs.muplayer.muplayer.data.properties.StreamPropertiesSource;

import java.io.InputStream;

public class LogConfig extends PropertiesInfo<InputStream> {

    @Getter
    private static LogConfig instance = new LogConfig();

    protected LogConfig() {
        super(new StreamPropertiesSource(ResourceFiles.LOG_CONFIG_RES_PATH));
    }

    @Override
    protected void loadDefaultData() {

    }
}
