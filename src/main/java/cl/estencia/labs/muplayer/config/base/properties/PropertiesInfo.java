package cl.estencia.labs.muplayer.config.base.properties;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class PropertiesInfo<T> {
    @Getter
    protected final PropertiesSource<T> propertiesSource;
    protected final Properties properties;

    protected PropertiesInfo(PropertiesSource<T> propertiesSource) {
        this.propertiesSource = propertiesSource;
        this.properties = new Properties();
        try {
            if (!this.propertiesSource.validate(properties)) {
                loadDefaultData();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    protected abstract void loadDefaultData();

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public Set<String> getPropertyNames() {
        return properties.stringPropertyNames().stream().sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Map<Object, Object> getProperties() {
        return new TreeMap<>(properties);
    }

    public void setProperty(String key, String value) throws Exception {
        properties.setProperty(key, value);
        propertiesSource.saveData(properties);
    }

}