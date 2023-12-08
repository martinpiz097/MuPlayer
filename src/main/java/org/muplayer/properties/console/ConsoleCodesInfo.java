package org.muplayer.properties.console;

import lombok.Getter;
import org.muplayer.properties.PropertiesInfo;
import org.muplayer.properties.StreamPropertiesSource;

import java.io.InputStream;

import static org.muplayer.properties.PropertiesFiles.CONSOLE_CODES_RES_PATH;

public class ConsoleCodesInfo extends PropertiesInfo<InputStream> {
    @Getter
    private static final ConsoleCodesInfo instance = new ConsoleCodesInfo();

    private ConsoleCodesInfo() {
        super(new StreamPropertiesSource(CONSOLE_CODES_RES_PATH));
    }

    @Override
    protected void loadDefaultData() {

    }
}
