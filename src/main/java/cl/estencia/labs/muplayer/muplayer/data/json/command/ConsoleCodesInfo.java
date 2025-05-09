package cl.estencia.labs.muplayer.muplayer.data.json.command;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import cl.estencia.labs.muplayer.muplayer.console.ConsoleOrderCode;
import cl.estencia.labs.muplayer.muplayer.data.json.JsonInfo;
import cl.estencia.labs.muplayer.muplayer.data.json.InternalJsonSource;
import cl.estencia.labs.muplayer.muplayer.data.json.command.model.ConsoleCodeResult;
import cl.estencia.labs.muplayer.muplayer.data.json.command.model.ConsoleCodesData;

import java.util.LinkedList;

import static cl.estencia.labs.muplayer.muplayer.data.properties.ResourceFiles.CONSOLE_PLAYER_CODES_RES_PATH;

public class ConsoleCodesInfo extends JsonInfo<String, LinkedList<ConsoleCodesData>> {

    private final ConsoleCodeResult consoleCodeResult;

    @Getter
    private static final ConsoleCodesInfo instance = new ConsoleCodesInfo();

    private ConsoleCodesInfo() {
        super(new InternalJsonSource<>(
                CONSOLE_PLAYER_CODES_RES_PATH,
                new TypeReference<>() {}, false));
        this.consoleCodeResult = new ConsoleCodeResult();
    }

    @Override
    protected void loadDefaultData() {

    }

    public ConsoleOrderCode getConsoleOrderCodeByCmdOrder(String order) {
        try {
            ConsoleOrderCode consoleOrderCode = consoleCodeResult.getOrderCodeByOrder(order);
            if (consoleOrderCode == null) {
                var data = jsonSource.getData();
                consoleOrderCode = data.parallelStream()
                        .filter(consoleCodesData -> consoleCodesData.hasOrder(order))
                        .findFirst()
                        .map(ConsoleCodesData::getCode)
                        .orElse(null);

                if (consoleOrderCode != null) {
                    consoleCodeResult.addCodeSearchResult(order, consoleOrderCode.name());
                }
            }
            return consoleOrderCode;
        } catch (Exception e) {
            return null;
        }
    }

}
