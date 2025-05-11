package cl.estencia.labs.muplayer.data.reader;

import lombok.Getter;
import cl.estencia.labs.muplayer.data.ResourceFiles;
import cl.estencia.labs.muplayer.data.reader.base.properties.PropertiesInfo;
import cl.estencia.labs.muplayer.data.reader.base.properties.source.StreamPropertiesSource;

import java.io.InputStream;

public class MessagesInfoReader extends PropertiesInfo<InputStream> {
    @Getter
    private static final MessagesInfoReader instance = new MessagesInfoReader();

    private MessagesInfoReader() {
        super(new StreamPropertiesSource(ResourceFiles.MESSAGES_RES_PATH));
    }

    @Override
    protected void loadDefaultData() {

    }
}
