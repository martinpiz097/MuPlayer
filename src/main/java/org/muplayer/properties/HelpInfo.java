package org.muplayer.properties;

import org.muplayer.util.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.muplayer.properties.PropertiesFilesInfo.HELP_FILE_PATH;

public class HelpInfo {
    private final Properties properties;

    private static final HelpInfo instance = new HelpInfo();

    public static HelpInfo getInstance() {
        return instance;
    }

    private HelpInfo() {
        properties = new Properties();
        loadData();
    }

    private void loadData() {
        try {
            final byte[] resourceBytes = IOUtil.getBytesFromStream(
                    getClass().getResourceAsStream(HELP_FILE_PATH));
            properties.load(new ByteArrayInputStream(resourceBytes));
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
