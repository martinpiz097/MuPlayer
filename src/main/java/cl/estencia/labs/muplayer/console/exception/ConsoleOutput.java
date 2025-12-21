package cl.estencia.labs.muplayer.console.exception;

import cl.estencia.labs.muplayer.console.command.Command;
import cl.estencia.labs.muplayer.console.enums.OutputType;
import lombok.Getter;
import lombok.Setter;
import org.orangelogger.sys.ConsoleColor;
import org.orangelogger.sys.Logger;

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
        return sbOutputMsg.length() > 0;
    }

    public void appendOutput(Object output, OutputType outputType) {
        if (outputType != null) {
            switch (outputType) {
                case info:
                    sbOutputMsg.append(Logger.INFOCOLOR);
                    break;
                case warn:
                    sbOutputMsg.append(Logger.WARNINGCOLOR);
                    break;
                case error:
                    sbOutputMsg.append(Logger.ERRORCOLOR);
                    break;
            }
        }
        sbOutputMsg.append(output).append(ConsoleColor.RESET).append('\n');
    }

    public String getOutputMsg() {
        return sbOutputMsg.length() > 0 ? sbOutputMsg.toString() : null;
    }

    public void setOutputObject(Object outputObject) {
        this.outputObject = outputObject != null ? outputObject : getOutputMsg();
    }

    public void setOutputAsOutputMsg() {
        setOutputObject(null);
    }
}
