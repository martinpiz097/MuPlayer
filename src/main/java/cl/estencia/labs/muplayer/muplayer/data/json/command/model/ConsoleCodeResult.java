package cl.estencia.labs.muplayer.muplayer.data.json.command.model;

import lombok.Data;
import cl.estencia.labs.muplayer.muplayer.console.ConsoleOrderCode;

import java.util.Map;
import java.util.TreeMap;

@Data
public class ConsoleCodeResult {
    private final Map<String, String> mapConsoleCodes;

    public ConsoleCodeResult() {
        this.mapConsoleCodes = new TreeMap<>();
    }

    public void addCodeSearchResult(String order, String code) {
        mapConsoleCodes.put(order, code);
    }

    public ConsoleOrderCode getOrderCodeByOrder(String order) {
        try {
            return ConsoleOrderCode.valueOf(mapConsoleCodes.get(order));
        } catch (Exception e) {
            return null;
        }
    }

}
