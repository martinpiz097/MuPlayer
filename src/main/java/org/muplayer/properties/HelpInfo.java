package org.muplayer.properties;

import org.muplayer.util.DataUtil;
import org.muplayer.util.IOUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.muplayer.properties.PropertiesFilesInfo.HELP_FILE_PATH;
import static org.muplayer.properties.PropertiesFilesInfo.HELP_FILE_RES_PATH;

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
            properties.load(DataUtil.getResourceAsStream(HELP_FILE_RES_PATH));
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
