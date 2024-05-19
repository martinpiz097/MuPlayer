package org.muplayer.data.json.command;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import org.muplayer.console.ConsoleOrderCode;
import org.muplayer.data.json.JsonInfo;
import org.muplayer.data.json.InternalJsonSource;
import org.muplayer.data.json.command.model.ConsoleCodesData;
import org.muplayer.data.properties.ResourceFiles;

import java.util.Map;
import java.util.TreeMap;

public class ConsoleCodesInfo extends JsonInfo<String, TreeMap<String, ConsoleCodesData>> {
    @Getter
    private static final ConsoleCodesInfo instance = new ConsoleCodesInfo();

    private ConsoleCodesInfo() {
        super(new InternalJsonSource<>(
                ResourceFiles.CONSOLE_PLAYER_CODES_RES_PATH,
                new TypeReference<>() {}));
    }

    @Override
    protected void loadDefaultData() {

    }

    public ConsoleOrderCode getConsoleOrderCodeByCmdOrder(String order) {
        try {
            Map<String, ConsoleCodesData> data = jsonSource.getData();

            return data.entrySet()
                    .parallelStream()
                    .filter(entry -> entry.getValue().hasOrder(order))
                    .findFirst()
                    .map(entry -> ConsoleOrderCode.valueOf(entry.getKey()))
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public ConsoleCodesData getCodesData(ConsoleOrderCode consoleOrderCode) {
        Map<String, ConsoleCodesData> data = jsonSource.getData();
        return data.get(consoleOrderCode.name());
    }

    public String getCodesByOrderCode(ConsoleOrderCode consoleOrderCode) {
        ConsoleCodesData consoleCodesData = getCodesData(consoleOrderCode);
        if (consoleCodesData != null) {
            return consoleCodesData.getJoinedOrders(",");
        }
        else {
            return null;
        }
    }
    
    private String getHelpInfoByOrderCode(ConsoleOrderCode consoleOrderCode) {
        ConsoleCodesData consoleCodesData = getCodesData(consoleOrderCode);
        if (consoleCodesData != null) {
            return consoleCodesData.getHelpInfo();
        }
        else {
            return null;
        }
    }
}
