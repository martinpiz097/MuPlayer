package org.muplayer.properties;

import java.util.Properties;

public abstract class PropertiesSource<T> {
    protected final T source;

    public PropertiesSource(T source) {
        this.source = source;
    }

    public Properties loadData() throws Exception {
        final Properties props = new Properties();
        loadData(props);
        return props;
    }

    public abstract boolean exists();

    public abstract void validate() throws Exception;

    public abstract void loadData(Properties properties) throws Exception;

    public void saveData() throws Exception {
        saveData(new Properties(), null);
    }

    public void saveData(String comments) throws Exception {
        saveData(new Properties(), comments);
    }

    public abstract void saveData(Properties properties, String comments) throws Exception;

}
