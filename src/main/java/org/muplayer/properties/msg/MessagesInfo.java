package org.muplayer.properties.msg;

import lombok.Getter;
import org.muplayer.properties.PropertiesInfo;
import org.muplayer.properties.StreamPropertiesSource;

import java.io.InputStream;

import static org.muplayer.properties.PropertiesFiles.MESSAGES_RES_PATH;

public class MessagesInfo extends PropertiesInfo<InputStream> {
    @Getter
    private static final MessagesInfo instance = new MessagesInfo();

    private MessagesInfo() {
        super(new StreamPropertiesSource(MESSAGES_RES_PATH));
    }

    @Override
    protected void loadDefaultData() {

    }
}
