package cl.estencia.labs.muplayer.data.reader;

import lombok.Getter;
import cl.estencia.labs.muplayer.data.ResourceFiles;
import cl.estencia.labs.muplayer.data.reader.base.properties.PropertiesInfo;
import cl.estencia.labs.muplayer.data.reader.base.properties.source.StreamPropertiesSource;

import java.io.InputStream;

public class MuPlayerInfoReader extends PropertiesInfo<InputStream> {
    @Getter
    private static final MuPlayerInfoReader instance = new MuPlayerInfoReader();

    private MuPlayerInfoReader() {
        super(new StreamPropertiesSource(ResourceFiles.MUPLAYER_INFO_RES_PATH));
    }

    @Override
    protected void loadDefaultData() {

    }
}
