package org.muplayer.properties;

import static org.muplayer.properties.PropertiesFiles.MUPLAYER_INFO_RES_PATH;

public class MuPlayerInfo extends PropertiesInfo {
    private static final MuPlayerInfo instance = new MuPlayerInfo();

    public static MuPlayerInfo getInstance() {
        return instance;
    }

    private MuPlayerInfo() {
        super(MUPLAYER_INFO_RES_PATH);
    }
}
