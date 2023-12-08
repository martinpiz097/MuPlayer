package org.muplayer.properties.help;

import lombok.Getter;
import org.muplayer.properties.PropertiesFiles;
import org.muplayer.properties.PropertiesInfo;
import org.muplayer.properties.StreamPropertiesSource;

import java.io.InputStream;

public class HelpInfo extends PropertiesInfo<InputStream> {
    @Getter
    private static final HelpInfo instance = new HelpInfo();

    protected HelpInfo() {
        super(new StreamPropertiesSource(PropertiesFiles.HELP_RES_PATH));
    }

    @Override
    protected void loadDefaultData() {

    }
}
