package cl.estencia.labs.muplayer.config.base.properties.source;

import cl.estencia.labs.muplayer.config.ResourceFiles;
import cl.estencia.labs.muplayer.config.base.properties.PropertiesSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class StreamPropertiesSource extends PropertiesSource<InputStream> {
    public StreamPropertiesSource(String sourcePath) {
        super(ResourceFiles.getResStream(sourcePath));
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
