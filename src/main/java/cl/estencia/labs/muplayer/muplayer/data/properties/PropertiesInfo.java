package cl.estencia.labs.muplayer.muplayer.data.properties;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

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
            e.printStackTrace();
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
        Map<Object, Object> mapProperties = new TreeMap<>();
        mapProperties.putAll(properties);

        return mapProperties;
    }

    public void setProperty(String key, String value) throws Exception {
        properties.setProperty(key, value);
        propertiesSource.saveData(properties);
    }
}