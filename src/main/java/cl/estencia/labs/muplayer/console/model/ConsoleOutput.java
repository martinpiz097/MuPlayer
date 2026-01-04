package cl.estencia.labs.muplayer.console.model;

import cl.estencia.labs.muplayer.console.command.Command;
import cl.estencia.labs.muplayer.console.common.enums.OutputType;
import cl.estencia.labs.muplayer.console.util.ConsoleUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsoleOutput {
    private final Command cmd;
    private Object outputObject;
    private final StringBuilder sbOutputMsg;

    public ConsoleOutput(String cmdString) {
        this(new Command(cmdString));
    }

    public ConsoleOutput(Command cmd) {
        this.cmd = cmd;
        this.sbOutputMsg = new StringBuilder();
    }

    public boolean hasOutput() {
        return !sbOutputMsg.isEmpty();
    }

    public void append(Object output) {
        sbOutputMsg.append(output);
    }

    public void append(Object output, OutputType outputType) {
        String coloredStringLine = ConsoleUtil.coloredStringLine(output, outputType, true);
        sbOutputMsg.append(coloredStringLine);
    }

    public String getOutputMsg() {
        return !sbOutputMsg.isEmpty() ? sbOutputMsg.toString() : null;
    }

    public void setOutputObject(Object outputObject) {
        this.outputObject = outputObject != null ? outputObject : getOutputMsg();
    }

    public void setOutputAsOutputMsg() {
        setOutputObject(null);
    }
}
