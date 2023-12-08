package org.muplayer.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class StreamPropertiesSource extends PropertiesSource<InputStream> {
    public StreamPropertiesSource(String sourcePath) {
        super(PropertiesFiles.getResStream(sourcePath));
    }

    @Override
    public boolean exists() {
        try {
            return source != null && source.available() > 0;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void validate() throws Exception {

    }

    @Override
    public void loadData(Properties properties) throws Exception {
        properties.load(source);
    }

    @Override
    public void saveData(Properties properties, String comments) throws Exception {

    }
}
