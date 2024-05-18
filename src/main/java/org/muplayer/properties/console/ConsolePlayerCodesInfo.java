package org.muplayer.properties.console;

import lombok.Getter;
import org.muplayer.properties.PropertiesInfo;
import org.muplayer.properties.StreamPropertiesSource;

import java.io.InputStream;

import static org.muplayer.properties.PropertiesFiles.CONSOLE_PLAYER_CODES_RES_PATH;

public class ConsolePlayerCodesInfo extends PropertiesInfo<InputStream> {
    @Getter
    private static final ConsolePlayerCodesInfo instance = new ConsolePlayerCodesInfo();

    private ConsolePlayerCodesInfo() {
        super(new StreamPropertiesSource(CONSOLE_PLAYER_CODES_RES_PATH));
    }

    @Override
    protected void loadDefaultData() {

    }
}
