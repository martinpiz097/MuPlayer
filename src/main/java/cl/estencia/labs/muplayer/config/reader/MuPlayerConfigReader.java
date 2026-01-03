package cl.estencia.labs.muplayer.config.reader;

import cl.estencia.labs.muplayer.config.ResourceFiles;
import cl.estencia.labs.muplayer.config.base.properties.PropertiesInfo;
import cl.estencia.labs.muplayer.config.base.properties.source.StreamPropertiesSource;
import cl.estencia.labs.muplayer.core.common.enums.ConsoleHeaderMode;
import lombok.Getter;

import java.io.InputStream;

public class MuPlayerConfigReader extends PropertiesInfo<InputStream> {
    @Getter
    private static final MuPlayerConfigReader instance = new MuPlayerConfigReader();

    private MuPlayerConfigReader() {
        super(new StreamPropertiesSource(ResourceFiles.MUPLAYER_CONFIG_RES_PATH));
    }

    @Override
    protected void loadDefaultData() {

    }
}
