package cl.estencia.labs.muplayer;

import cl.estencia.labs.ebot.bus.MessageBus;
import cl.estencia.labs.muplayer.config.model.MessagesInfoKeys;
import cl.estencia.labs.muplayer.config.reader.MessagesInfoReader;
import cl.estencia.labs.muplayer.config.model.LogConfigKeys;
import cl.estencia.labs.muplayer.config.model.MuPlayerConfigKeys;
import cl.estencia.labs.muplayer.config.reader.LogConfigReader;
import cl.estencia.labs.muplayer.config.reader.MuPlayerConfigReader;
import cl.estencia.labs.muplayer.console.runner.ConsoleRunner;
import cl.estencia.labs.muplayer.console.runner.DaemonRunner;
import cl.estencia.labs.muplayer.console.runner.LocalRunner;
import cl.estencia.labs.muplayer.core.bus.util.MessageBusUtil;
import cl.estencia.labs.muplayer.core.cache.CacheManager;
import cl.estencia.labs.muplayer.core.cache.CacheVar;
import cl.estencia.labs.muplayer.core.thread.TaskRunner;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.Level;
import java.util.logging.LogManager;

@Slf4j
public class Main {

    static void main(String[] args) throws Exception {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        setJvmAppName();
        MessagesInfoReader messagesInfoReader = MessagesInfoReader.getInstance();
        CacheManager globalCache = CacheManager.getGlobalCache();
        MessageBus messageBus = MessageBusUtil.getMessageBus();

        try {
            loadLogConfig();

            ConsoleRunner consoleRunner = null;
            if (args.length == 0) {
                consoleRunner = new LocalRunner();
            } else {
                switch (args.length) {
                    case 1:
                        String firstArg = args[0].trim();
                        if (firstArg.startsWith("-")) {
                            throw new NullPointerException(messagesInfoReader.getProperty(MessagesInfoKeys.PROPERTY_NOT_FOUND_MSG));
                        } else {
                            consoleRunner = new LocalRunner(firstArg);
                        }
                        break;
                    case 2:
                        firstArg = args[0].trim();
                        if (firstArg.startsWith("-")) {
                            if (firstArg.equals("-l")) {
                                consoleRunner = new LocalRunner(args[1]);
                            } else if (firstArg.equals("-d")) {
                                consoleRunner = new DaemonRunner(args[1]);
                            } else {
                                throw new NullPointerException("Arg " + firstArg + "not recognized");
                            }
                        } else {
                            throw new NullPointerException("Arg " + firstArg + "not recognized");
                        }
                        break;
                }
            }
            if (consoleRunner != null) {
                TaskRunner.execute(consoleRunner, consoleRunner.getClass().getSimpleName());
                globalCache.saveValue(CacheVar.RUNNER, consoleRunner);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error on MuPlayer class", e);

            messageBus.shutdown();
        }
    }

    private static void setJvmAppName() {
        MuPlayerConfigReader muPlayerInfo = MuPlayerConfigReader.getInstance();
        System.setProperty("jvm.name", "MuPlayer v" + muPlayerInfo.getProperty(
                MuPlayerConfigKeys.MU_PLAYER_VERSION));
    }

    private static void loadLogConfig() {
        final LogConfigReader logConfigReader = LogConfigReader.getInstance();
        final String levelName = logConfigReader.getProperty(LogConfigKeys.JAVA_LOG_LEVEL);
        final Level logLevel = Level.parse(levelName);
        final LogManager logManager = LogManager.getLogManager();

        logManager.getLoggerNames().asIterator().forEachRemaining(logName ->
                logManager.getLogger(logName).setLevel(logLevel)
        );
    }

}
