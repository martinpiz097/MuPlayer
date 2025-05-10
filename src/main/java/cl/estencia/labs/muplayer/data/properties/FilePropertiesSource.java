package cl.estencia.labs.muplayer.data.properties;

import cl.estencia.labs.muplayer.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

public class FilePropertiesSource extends PropertiesSource<File> {
    public FilePropertiesSource(String sourcePath) {
        this(new File(sourcePath));
    }

    public FilePropertiesSource(File source) {
        super(source);
    }

    @Override
    public boolean exists() {
        return source != null && source.exists();
    }

    @Override
    public void loadData(Properties properties) throws Exception {
        properties.load(IOUtil.getFileBufferedReader(source));
    }

    @Override
    public void saveData(Properties properties, String comments) throws Exception {
        properties.store(new FileOutputStream(source), comments);
    }

}
