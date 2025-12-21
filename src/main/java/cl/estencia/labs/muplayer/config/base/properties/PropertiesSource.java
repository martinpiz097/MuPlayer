package cl.estencia.labs.muplayer.config.base.properties;

import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
public abstract class PropertiesSource<T> {
    protected final T source;

    public PropertiesSource(T source) {
        this.source = source;
    }

    public boolean validate(Properties properties) {
        try {
            boolean valid = exists();
            if (valid) {
                loadData(properties);
            } else {
                // se crea la fuente de properties
                saveData(properties);
            }
            return valid;
        } catch (Exception e) {
            log.error("Error on PropertiesSource", e);
            return false;
        }
    }

    public abstract boolean exists();

    public abstract void loadData(Properties properties) throws Exception;

    public void saveData(Properties properties) throws Exception {
        saveData(properties, null);
    }

    public abstract void saveData(Properties properties, String comments) throws Exception;

}
