package cl.estencia.labs.muplayer.muplayer.data.properties.msg;

import lombok.Getter;
import cl.estencia.labs.muplayer.muplayer.data.properties.ResourceFiles;
import cl.estencia.labs.muplayer.muplayer.data.properties.PropertiesInfo;
import cl.estencia.labs.muplayer.muplayer.data.properties.StreamPropertiesSource;

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
