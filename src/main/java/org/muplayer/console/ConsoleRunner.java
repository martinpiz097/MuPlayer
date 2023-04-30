package org.muplayer.console;

import lombok.Getter;
import org.muplayer.audio.player.MusicPlayer;
import org.muplayer.audio.player.Player;
import org.muplayer.system.SysInfo;
import org.orangelogger.sys.Logger;
import org.orangelogger.sys.SystemUtil;

import java.io.*;
import java.util.Scanner;

public abstract class ConsoleRunner implements Runnable {
    @Getter
    protected final Player player;
    protected final ConsoleInterpreter interpreter;
    protected final Scanner scanner;

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
    }

    protected void printHeader() {
        final FileOutputStream stdout = SystemUtil.getStdout();
        try {
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
