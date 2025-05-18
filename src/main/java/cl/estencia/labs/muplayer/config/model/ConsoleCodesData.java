package cl.estencia.labs.muplayer.config.model;

import cl.estencia.labs.muplayer.console.enums.ConsoleOrderCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.StringJoiner;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsoleCodesData {
    @JsonProperty("code")
    private ConsoleOrderCode code;

    @JsonProperty("orders")
    private String[] orders;

    @JsonProperty("helpInfo")
    private String helpInfo;

    public boolean hasOrder(String order) {
        if (orders != null && orders.length > 0) {
            for (int i = 0; i < orders.length; i++) {
                if (orders[i].equalsIgnoreCase(order)) {
                    return true;
                }
            }
            return false;
        }
        else {
            return false;
        }
    }

    public String getJoinedOrders() {
        return getJoinedOrders("");
    }

    public String getJoinedOrders(String delimiter) {
        if (orders != null && orders.length > 0) {
            StringJoiner joiner = new StringJoiner(delimiter);
            for (int i = 0; i < orders.length; i++) {
                joiner.add(orders[i]);
            }
            return joiner.toString();
        }
        else {
            return "";
        }
    }
}
