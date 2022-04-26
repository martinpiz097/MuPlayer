package org.muplayer.properties;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.muplayer.properties.ConfigInfo.HELP_FILE_NAME;

public class AppInfo {
    private Properties properties;
    private boolean cacheMode;

    private static final HelpInfo instance = new HelpInfo();

    public static HelpInfo getInstance() {
        return instance;
    }

    public AppInfo() {
        cacheMode = false;
        try {
            checkFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkFile() throws IOException {
        properties = new Properties();
        loadData();
    }

    // revisa si se han hecho cambios, si esta propiedad existe y tiene texto valido se deja tal cual
    private void checkProperty(String key, String defaultValue) {
        final String property = properties.getProperty(key);
        if (property == null || property.isEmpty()) {
            properties.setProperty(key, defaultValue);
        }
    }

    private void loadData() {
        try {
            properties = new Properties();
            properties.load(getClass().getResourceAsStream(HELP_FILE_NAME));
            //properties.load(new FileReader(fileProps));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCacheMode(boolean cacheMode) {
        this.cacheMode = cacheMode;
        if (cacheMode && properties == null) {
            loadData();
        }
        else {
            properties = null;
            System.gc();
        }
    }

    public String getProperty(String key) {
        if (cacheMode) {
            return properties.get(key).toString();
        }
        else {
            loadData();
            String value = properties.get(key).toString();
            properties = null;
            return value;
        }
    }

    public Set<String> getPropertyNames() {
        Set<String> stringsPropNames;
        if (cacheMode)
            stringsPropNames = properties.stringPropertyNames();
        else {
            loadData();
            stringsPropNames = properties.stringPropertyNames();
            properties = null;
        }

        return stringsPropNames.stream().sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
