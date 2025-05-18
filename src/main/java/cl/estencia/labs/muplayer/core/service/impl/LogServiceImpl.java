package cl.estencia.labs.muplayer.core.service.impl;

import cl.estencia.labs.muplayer.core.service.LogService;
import org.orangelogger.sys.Logger;
import org.orangelogger.sys.SystemUtil;

import java.io.FileOutputStream;
import java.io.IOException;

public class LogServiceImpl implements LogService {

    private final FileOutputStream fileOutputStream;

    public LogServiceImpl() {
        fileOutputStream = SystemUtil.getStdout();
    }

    @Override
    public void errorLog(String message) {
        log(message, Logger.ERRORCOLOR);
    }

    @Override
    public void warningLog(String message) {
        log(message, Logger.WARNINGCOLOR);
    }

    @Override
    public void log(String message, String color) {
        try {
            Logger logger = Logger.getLogger(this, message);
            String coloredMsg = logger.getColoredMsg(color);
            byte[] bytes = coloredMsg.getBytes();

            fileOutputStream.write(bytes);
            fileOutputStream.flush();
        } catch (IOException e) {
            Logger.getLogger(this, e.getClass().getSimpleName(), e.getMessage()).error();
        }
    }
}
