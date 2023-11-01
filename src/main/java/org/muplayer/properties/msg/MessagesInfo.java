package org.muplayer.properties;

import static org.muplayer.properties.PropertiesFiles.MESSAGES_RES_PATH;

public class MessagesInfo extends PropertiesInfo {
    private static final MessagesInfo instance = new MessagesInfo();

    public static MessagesInfo getInstance() {
        return instance;
    }

    private MessagesInfo() {
        super(MESSAGES_RES_PATH);
    }

}
