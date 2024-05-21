package org.muplayer.console.runner;

import lombok.Getter;
import org.muplayer.audio.player.MusicPlayer;
import org.muplayer.audio.player.Player;
import org.muplayer.console.ConsoleExecution;
import org.muplayer.console.ConsoleInterpreter;
import org.muplayer.data.CacheManager;
import org.orangelogger.sys.Logger;
import org.orangelogger.sys.SystemUtil;

import java.io.*;
import java.util.Scanner;

public abstract class ConsoleRunner implements Runnable {
    @Getter
    protected final Player player;
    protected final ConsoleInterpreter interpreter;
    protected final Scanner scanner;
    protected final CacheManager globalCacheManager;

    protected static final String LINEHEADER = "[MuPlayer]> ";

    public ConsoleRunner() throws FileNotFoundException {
        this((File) null);
    }

    public ConsoleRunner(String folder) throws FileNotFoundException {
        this(new File(folder));
    }

    public ConsoleRunner(File rootFolder) throws FileNotFoundException {
        this(new MusicPlayer(rootFolder));
    }

    public ConsoleRunner(Player player) {
        this.player = player;
        interpreter = new ConsoleInterpreter(player);
        scanner = new Scanner(System.in);
        globalCacheManager = CacheManager.getGlobalCache();
    }

    protected void printHeader() {
        try {
            final FileOutputStream stdout = SystemUtil.getStdout();
            stdout.write(Logger.getLogger(this, LINEHEADER)
                    .getColoredMsg(Logger.INFOCOLOR).getBytes());
            stdout.flush();
        } catch (IOException e) {
            Logger.getLogger(this, e.getClass().getSimpleName(), e.getMessage()).error();
        }
    }

    public ConsoleExecution execCommand(String strCmd) {
        try {
            return interpreter.executeCommand(strCmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
