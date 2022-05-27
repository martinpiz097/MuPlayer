package org.muplayer.main;

import org.muplayer.audio.player.MusicPlayer;
import org.muplayer.console.ConsoleRunner;
import org.muplayer.net.DaemonRunner;
import org.muplayer.properties.*;
import org.muplayer.thread.TaskRunner;

public class MuPlayer {
    public static void main(String[] args) {
        PropertiesFilesInfo.getJarParentPath();
        try {
            if (args.length == 0) {
                final String defaultRootPath = ConfigInfo.getInstance().get(ConfigInfoKeys.DEFAULT_ROOT_FOLDER);
                if (defaultRootPath == null) {
                    throw new NullPointerException(MessagesInfo.getInstance().getProperty(MessagesInfoKeys.PROPERTY_NOT_FOUND_MSG));
                }
                else
                    TaskRunner.execute(new ConsoleRunner(defaultRootPath));
            }
            else switch (args.length) {
                case 1:
                    String firstArg = args[0].trim();
                    if (firstArg.startsWith("-"))
                        throw new NullPointerException(MessagesInfo.getInstance().getProperty(MessagesInfoKeys.PROPERTY_NOT_FOUND_MSG));
                    else
                        TaskRunner.execute(new ConsoleRunner(firstArg));
                    break;
                case 2:
                    firstArg = args[0].trim();
                    if (firstArg.startsWith("-")) {
                        if (firstArg.equals("-l"))
                            TaskRunner.execute(new ConsoleRunner(args[1]));
                        else if (firstArg.equals("-d"))
                            TaskRunner.execute(new DaemonRunner(new MusicPlayer(args[1])));
                        else
                            throw new NullPointerException("Arg "+firstArg +"not recognized");
                    }
                    else {
                        throw new NullPointerException("Arg "+firstArg +"not recognized");
                    }
                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
            //Logger.getLogger(ConsolePlayer.class, e.getClass().getSimpleName(), e.getMessage()).error();
        }
    }
}
