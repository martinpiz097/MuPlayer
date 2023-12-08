package org.muplayer.properties;

import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class PropertiesInfo<T> {
    @Getter
    protected final PropertiesSource<T> propsSource;
    protected Properties props;

    protected PropertiesInfo(PropertiesSource<T> propertiesSource) {
        this.propsSource = propertiesSource;
        this.props = new Properties();
        try {
            this.propsSource.validate();
            loadDefaultData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void loadDefaultData();

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public Set<String> getPropertyNames() {
        return props.stringPropertyNames().stream().sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setProperty(String key, String value) throws Exception {
        props.setProperty(key, value);
        propsSource.saveData();
    }
}