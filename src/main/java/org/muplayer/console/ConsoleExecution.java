package org.muplayer.console;

import lombok.Data;

@Data
public class ConsoleExecution {
    private String cmd;
    private Object output;
    private String outputType;

    public boolean hasOutput() {
        return output != null;
    }
}
