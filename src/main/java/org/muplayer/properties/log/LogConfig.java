package org.muplayer.properties.log;

import org.muplayer.properties.PropertiesInfo;
import org.muplayer.properties.PropertiesSource;

import java.io.InputStream;

public class LogConfig extends PropertiesInfo<InputStream> {
    protected LogConfig(PropertiesSource<InputStream> propertiesSource) {
        super(propertiesSource);
    }

    @Override
    protected void loadDefaultData() {

    }
}
