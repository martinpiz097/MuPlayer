package cl.estencia.labs.muplayer;

import cl.estencia.labs.muplayer.cache.CacheManager;
import cl.estencia.labs.muplayer.cache.CacheVar;
import cl.estencia.labs.muplayer.config.model.LogConfigKeys;
import cl.estencia.labs.muplayer.config.model.MessagesInfoKeys;
import cl.estencia.labs.muplayer.config.reader.LogConfigReader;
import cl.estencia.labs.muplayer.config.reader.MessagesInfoReader;
import cl.estencia.labs.muplayer.console.runner.ConsoleRunner;
import cl.estencia.labs.muplayer.console.runner.DaemonRunner;
import cl.estencia.labs.muplayer.console.runner.LocalRunner;
import cl.estencia.labs.muplayer.thread.TaskRunner;
import lombok.extern.java.Log;

import java.util.logging.Level;
import java.util.logging.LogManager;

@Log
public class Main {

    public static void main(String[] args) {
        MessagesInfoReader messagesInfoReader = MessagesInfoReader.getInstance();
        CacheManager globalCache = CacheManager.getGlobalCache();

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
            log.severe("Error on MuPlayer class: " + e);
        }
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
