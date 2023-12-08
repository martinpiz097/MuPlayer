package org.muplayer.properties.info;

import lombok.Getter;
import org.muplayer.properties.PropertiesInfo;
import org.muplayer.properties.StreamPropertiesSource;

import java.io.InputStream;

import static org.muplayer.properties.PropertiesFiles.MUPLAYER_INFO_RES_PATH;

public class MuPlayerInfo extends PropertiesInfo<InputStream> {
    @Getter
    private static final MuPlayerInfo instance = new MuPlayerInfo();

    private MuPlayerInfo() {
        super(new StreamPropertiesSource(MUPLAYER_INFO_RES_PATH));
    }

    @Override
    protected void loadDefaultData() {

    }
}
