package org.muplayer.data.properties.info;

import lombok.Getter;
import org.muplayer.data.properties.ResourceFiles;
import org.muplayer.data.properties.PropertiesInfo;
import org.muplayer.data.properties.StreamPropertiesSource;

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
