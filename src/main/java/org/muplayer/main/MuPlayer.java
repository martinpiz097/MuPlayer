package org.muplayer.main;

import org.muplayer.console.runner.ConsoleRunner;
import org.muplayer.console.runner.DaemonRunner;
import org.muplayer.console.runner.LocalRunner;
import org.muplayer.properties.ConfigInfo;
import org.muplayer.properties.ConfigInfoKeys;
import org.muplayer.properties.MessagesInfo;
import org.muplayer.properties.MessagesInfoKeys;
import org.muplayer.system.Global;
import org.muplayer.system.GlobalVar;
import org.muplayer.thread.TaskRunner;

public class MuPlayer {
    public static void main(String[] args) {
        try {
            ConsoleRunner consoleRunner = null;
            if (args.length == 0) {
                final String defaultRootPath = ConfigInfo.getInstance().get(ConfigInfoKeys.DEFAULT_ROOT_FOLDER);
                if (defaultRootPath == null)
                    throw new NullPointerException(MessagesInfo.getInstance().getProperty(MessagesInfoKeys.PROPERTY_NOT_FOUND_MSG));
                else
                    consoleRunner = new LocalRunner(defaultRootPath);
            }
            else switch (args.length) {
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
                            throw new NullPointerException("Arg "+firstArg +"not recognized");
                    }
                    else
                        throw new NullPointerException("Arg "+firstArg +"not recognized");
                    break;
            }
            if (consoleRunner != null) {
                TaskRunner.execute(consoleRunner);
                Global.getInstance().setVar(GlobalVar.RUNNER, consoleRunner);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Logger.getLogger(ConsolePlayer.class, e.getClass().getSimpleName(), e.getMessage()).error();
        }
    }
}
