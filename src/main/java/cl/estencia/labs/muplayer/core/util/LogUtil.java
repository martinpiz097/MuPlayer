package cl.estencia.labs.muplayer.core.util;

import org.slf4j.bridge.SLF4JBridgeHandler;

public class LogUtil {
    public static void redirectLogsThroughElogger() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }
}
