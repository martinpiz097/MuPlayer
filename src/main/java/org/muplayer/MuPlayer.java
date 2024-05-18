package org.muplayer;

import lombok.extern.java.Log;
import org.muplayer.console.runner.ConsoleRunner;
import org.muplayer.console.runner.DaemonRunner;
import org.muplayer.console.runner.LocalRunner;
import org.muplayer.properties.config.ConfigInfo;
import org.muplayer.properties.config.ConfigInfoKeys;
import org.muplayer.properties.console.ConsolePlayerCodesInfo;
import org.muplayer.properties.log.LogConfig;
import org.muplayer.properties.log.LogConfigKeys;
import org.muplayer.properties.msg.MessagesInfo;
import org.muplayer.properties.msg.MessagesInfoKeys;
import org.muplayer.system.Global;
import org.muplayer.system.GlobalVar;
import org.muplayer.thread.TaskRunner;
import org.muplayer.util.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static org.muplayer.properties.PropertiesFiles.LOG_CONFIG_RES_PATH;

@Log
public class MuPlayer {
    public static void main(String[] args) {
        try {
            ConsolePlayerCodesInfo.getInstance().getProperties();
            loadLogConfig();
            ConsoleRunner consoleRunner = null;
            if (args.length == 0) {
                final String defaultRootPath = ConfigInfo.getInstance().getProperty(ConfigInfoKeys.DEFAULT_MUSIC_FOLDER);
                if (defaultRootPath == null)
                    throw new NullPointerException(MessagesInfo.getInstance().getProperty(MessagesInfoKeys.PROPERTY_NOT_FOUND_MSG));
                else
                    consoleRunner = new LocalRunner(defaultRootPath);
            } else switch (args.length) {
                case 1:
                    String firstArg = args[0].trim();
                    if (firstArg.startsWith("-"))
                        throw new NullPointerException(MessagesInfo.getInstance().getProperty(MessagesInfoKeys.PROPERTY_NOT_FOUND_MSG));
                    else
                        consoleRunner = new LocalRunner(firstArg);
                    break;
                case 2:
                    firstArg = args[0].trim();
                    if (firstArg.startsWith("-")) {
                        if (firstArg.equals("-l"))
                            consoleRunner = new LocalRunner(args[1]);
                        else if (firstArg.equals("-d"))
                            consoleRunner = new DaemonRunner(args[1]);
                        else
                            throw new NullPointerException("Arg " + firstArg + "not recognized");
                    } else
                        throw new NullPointerException("Arg " + firstArg + "not recognized");
                    break;
            }
            if (consoleRunner != null) {
                TaskRunner.execute(consoleRunner, consoleRunner.getClass().getSimpleName());
                Global.getInstance().setVar(GlobalVar.RUNNER, consoleRunner);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.severe("Error on MuPlayer class: " + e);
            //Logger.getLogger(ConsolePlayer.class, e.getClass().getSimpleName(), e.getMessage()).error();
        }
    }

    private static void loadLogConfig() {
        final LogConfig logConfig = LogConfig.getInstance();
        final String levelName = logConfig.getProperty(LogConfigKeys.JAVA_LOG_LEVEL);
        final Level logLevel = Level.parse(levelName);
        final LogManager logManager = LogManager.getLogManager();

        logManager.getLoggerNames().asIterator().forEachRemaining(logName ->
                logManager.getLogger(logName).setLevel(logLevel)
        );
    }
}
