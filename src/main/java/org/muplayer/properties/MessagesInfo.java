package org.muplayer.properties;

import org.muplayer.util.DataUtil;

import java.io.IOException;
import java.util.Properties;

import static org.muplayer.properties.PropertiesFilesInfo.MESSAGES_FILE_RES_PATH;

public class MessagesInfo {
    private final Properties properties;
    private static final MessagesInfo instance = new MessagesInfo();

    public static MessagesInfo getInstance() {
        return instance;
    }

    private MessagesInfo() {
        properties = new Properties();
        loadData();
    }

    private void loadData() {
        try {
            properties.load(DataUtil.getResourceAsStream(MESSAGES_FILE_RES_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.get(key).toString();
    }
}
