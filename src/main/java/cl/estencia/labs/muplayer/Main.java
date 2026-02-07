package cl.estencia.labs.muplayer;

import cl.estencia.labs.muplayer.config.model.LogConfigKeys;
import cl.estencia.labs.muplayer.config.model.MessagesInfoKeys;
import cl.estencia.labs.muplayer.config.model.MuPlayerConfigKeys;
import cl.estencia.labs.muplayer.console.runner.ConsoleRunner;
import cl.estencia.labs.muplayer.console.runner.DaemonRunner;
import cl.estencia.labs.muplayer.console.runner.LocalRunner;
import cl.estencia.labs.muplayer.core.cache.CacheVar;
import cl.estencia.labs.muplayer.core.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.dbus.exceptions.DBusException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static cl.estencia.labs.muplayer.audio.common.constants.PlayerConstants.PLAYER_BUS;
import static cl.estencia.labs.muplayer.config.reader.ResourceReaders.*;
import static cl.estencia.labs.muplayer.core.cache.CacheManager.CACHE;

@Slf4j
public class Main {

    static void main(String[] args) {
        LogUtil.redirectLogsThroughElogger();
        setJvmAppName();

        try {
            loadLogConfig();
            ConsoleRunner consoleRunner = getConsoleRunnerFromParams(args);
            if (consoleRunner == null) {
                log.error("Unexpected parameters count: " + args.length);
                System.exit(0);
            }

            consoleRunner.start();
            CACHE.set(CacheVar.RUNNER, consoleRunner);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error on MuPlayer class", e);

            PLAYER_BUS.unsubscribeAll();
            PLAYER_BUS.shutdown();
        }
    }

    private static ConsoleRunner getConsoleRunnerFromParams(String[] args) throws IOException, DBusException {
        final int parametersCount = args.length;
        if (parametersCount == 0) {
            return new LocalRunner();
        }

        final String firstArg = args[0].trim();
        return switch (parametersCount) {
            case 1 -> {
                if (firstArg.startsWith("-")) {
                    throw new NullPointerException(MESSAGES_INFO_READER.getProperty(MessagesInfoKeys.PROPERTY_NOT_FOUND_MSG));
                }

                yield new LocalRunner(firstArg);
            }
            case 2 -> {
                if (!firstArg.startsWith("-")) {
                    throw new NullPointerException("Arg " + firstArg + " not recognized");
                }

                final String secondArg = args[1];
                yield switch (firstArg) {
                    case "-l" -> new LocalRunner(secondArg);
                    case "-d" -> new DaemonRunner(secondArg);
                    default -> throw new NullPointerException("Arg " + firstArg + " not recognized");
                };
            }
            default -> null;
        };
    }

    private static void setJvmAppName() {
        System.setProperty("jvm.name", "MuPlayer v" + MUPLAYER_CONFIG_READER.getProperty(
                MuPlayerConfigKeys.MU_PLAYER_VERSION));
    }

    private static void loadLogConfig() {
        final String levelName = LOG_CONFIG_READER.getProperty(LogConfigKeys.JAVA_LOG_LEVEL);
        final Level logLevel = Level.parse(levelName);
        final LogManager logManager = LogManager.getLogManager();

        logManager.getLoggerNames().asIterator().forEachRemaining(logName ->
                logManager.getLogger(logName).setLevel(logLevel)
        );
    }

}
