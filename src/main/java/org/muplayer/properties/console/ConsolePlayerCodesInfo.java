package org.muplayer.properties.console;

import lombok.Getter;
import org.muplayer.console.ConsolePlayerOrderCode;
import org.muplayer.properties.PropertiesInfo;
import org.muplayer.properties.StreamPropertiesSource;

import java.io.InputStream;
import java.util.stream.Collectors;

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

    public ConsolePlayerOrderCode getConsoleOrderCodeByCmdOrder(String code) {
        try {
            return ConsolePlayerOrderCode.valueOf(getProperty(code));
        } catch (Exception e) {
            return null;
        }
    }

    public String getCodesByOrderCode(ConsolePlayerOrderCode consolePlayerOrderCode) {
        return properties.entrySet().stream()
                .filter(entry -> entry.getValue().toString().equals(consolePlayerOrderCode.name()))
                .map(entry -> entry.getKey().toString())
                .collect(Collectors.joining(","));
    }
}
