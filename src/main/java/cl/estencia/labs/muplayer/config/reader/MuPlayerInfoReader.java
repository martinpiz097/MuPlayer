package cl.estencia.labs.muplayer.config.reader;

import cl.estencia.labs.muplayer.config.ResourceFiles;
import cl.estencia.labs.muplayer.config.base.properties.PropertiesInfo;
import cl.estencia.labs.muplayer.config.base.properties.source.StreamPropertiesSource;
import lombok.Getter;

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
