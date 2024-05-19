package org.muplayer.data.properties.log;

import lombok.Getter;
import org.muplayer.data.properties.ResourceFiles;
import org.muplayer.data.properties.PropertiesInfo;
import org.muplayer.data.properties.StreamPropertiesSource;

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
