package org.muplayer.properties;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.muplayer.properties.ConfigInfo.HELP_FILE_PATH;

public class HelpInfo {
    private final Properties properties;

    private static final HelpInfo instance = new HelpInfo();

    public static HelpInfo getInstance() {
        return instance;
    }

    public HelpInfo() {
        properties = new Properties();
        loadData();
    }

    private void checkProperty(String key, String defaultValue) {
        final String property = properties.getProperty(key);
        if (property == null || property.isEmpty()) {
            properties.setProperty(key, defaultValue);
        }
    }

    private void loadData() {
        try {
            properties.load(getClass().getResourceAsStream(HELP_FILE_PATH));
            System.out.println("");
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
