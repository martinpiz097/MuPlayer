package cl.estencia.labs.muplayer.muplayer.data.properties.info;

import lombok.Getter;
import cl.estencia.labs.muplayer.muplayer.data.properties.ResourceFiles;
import cl.estencia.labs.muplayer.muplayer.data.properties.PropertiesInfo;
import cl.estencia.labs.muplayer.muplayer.data.properties.StreamPropertiesSource;

import java.io.InputStream;

public class MuPlayerInfo extends PropertiesInfo<InputStream> {
    @Getter
    private static final MuPlayerInfo instance = new MuPlayerInfo();

    private MuPlayerInfo() {
        super(new StreamPropertiesSource(ResourceFiles.MUPLAYER_INFO_RES_PATH));
    }

    @Override
    protected void loadDefaultData() {

    }
}
