package org.muplayer.properties;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.muplayer.properties.PropertiesFilesInfo.INFO_FILE_PATH;

public class AppInfo {
    private final Properties properties;

    private static final AppInfo instance = new AppInfo();

    public static AppInfo getInstance() {
        return instance;
    }

    public AppInfo() {
        properties = new Properties();
        loadData();
    }

    private void loadData() {
        try {
            properties.load(getClass().getResourceAsStream(INFO_FILE_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.get(key).toString();
    }

    public Set<String> getPropertyNames() {
        return properties.stringPropertyNames().stream().sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
