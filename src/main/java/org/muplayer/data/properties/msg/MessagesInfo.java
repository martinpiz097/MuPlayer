package org.muplayer.data.properties.msg;

import lombok.Getter;
import org.muplayer.data.properties.ResourceFiles;
import org.muplayer.data.properties.PropertiesInfo;
import org.muplayer.data.properties.StreamPropertiesSource;

import java.io.InputStream;

public class MessagesInfo extends PropertiesInfo<InputStream> {
    @Getter
    private static final MessagesInfo instance = new MessagesInfo();

    private MessagesInfo() {
        super(new StreamPropertiesSource(ResourceFiles.MESSAGES_RES_PATH));
    }

    @Override
    protected void loadDefaultData() {

    }
}
