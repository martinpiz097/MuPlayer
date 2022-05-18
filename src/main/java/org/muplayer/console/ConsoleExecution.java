package org.muplayer.console;

import lombok.Data;
import org.orangelogger.sys.ConsoleColor;
import org.orangelogger.sys.Logger;

import static org.muplayer.console.OutputType.*;

@Data
public class ConsoleExecution {
    private String cmd;
    private final StringBuilder sbOutputMsg;

    public ConsoleExecution() {
        sbOutputMsg = new StringBuilder();
    }

    public boolean hasOutput() {
        return sbOutputMsg.length() > 0;
    }

    public void appendOutput(Object output, String outputType) {
        switch (outputType) {
            case INFO:
                sbOutputMsg.append(Logger.INFOCOLOR);
                break;
            case WARNING:
                sbOutputMsg.append(Logger.WARNINGCOLOR);
                break;
            case ERROR:
                sbOutputMsg.append(Logger.ERRORCOLOR);
                break;
        }
        sbOutputMsg.append(output).append(ConsoleColor.RESET).append('\n');
    }

    public String getOutput() {
        return sbOutputMsg.toString();
    }
}
