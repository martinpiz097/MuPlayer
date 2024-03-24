package org.muplayer.properties.log;

import lombok.Getter;
import org.muplayer.properties.PropertiesFiles;
import org.muplayer.properties.PropertiesInfo;
import org.muplayer.properties.PropertiesSource;
import org.muplayer.properties.StreamPropertiesSource;
import org.muplayer.properties.config.ConfigInfo;

import java.io.InputStream;

public class LogConfig extends PropertiesInfo<InputStream> {

    @Getter
    private static LogConfig instance = new LogConfig();

    protected LogConfig() {
        super(new StreamPropertiesSource(PropertiesFiles.LOG_CONFIG_RES_PATH));
    }

    @Override
    protected void loadDefaultData() {

    }
}
