package cl.estencia.labs.muplayer.config.model;

import cl.estencia.labs.muplayer.console.common.enums.ConsoleOrderCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
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
