package org.muplayer.properties;

public class HelpInfo extends PropertiesInfo {
    private static final HelpInfo instance = new HelpInfo();

    protected HelpInfo() {
        super(PropertiesFiles.HELP_RES_PATH);
    }

    public static HelpInfo getInstance() {
        return instance;
    }

}
