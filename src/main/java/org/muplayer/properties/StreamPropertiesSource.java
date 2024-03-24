package org.muplayer.properties;

import java.io.*;
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
    public void loadData(Properties properties) throws Exception {
        properties.load(source);
    }

    @Override
    public void saveData(Properties properties, String comments) throws Exception {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        baos.writeBytes(source.readAllBytes());
//        properties.store(baos, comments);
//        baos.close();
    }
}
