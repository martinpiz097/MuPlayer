package org.muplayer.console;

import lombok.Getter;
import org.muplayer.audio.player.MusicPlayer;
import org.muplayer.audio.player.Player;
import org.muplayer.system.SysInfo;
import org.orangelogger.sys.Logger;
import org.orangelogger.sys.SystemUtil;

import java.io.*;
import java.util.Scanner;

public class ConsoleRunner extends Thread {
    @Getter
    protected final Player player;
    protected final ConsoleInterpreter interpreter;
    protected final Scanner scanner;

    protected static final String LINEHEADER = "[MuPlayer]> ";

    public ConsoleRunner() throws FileNotFoundException {
        player = new MusicPlayer((File) null);
        interpreter = new ConsoleInterpreter(player);
        scanner = new Scanner(System.in);
    }

    public ConsoleRunner(File rootFolder) throws FileNotFoundException {
        player = new MusicPlayer(rootFolder);
        interpreter = new ConsoleInterpreter(player);
        scanner = new Scanner(System.in);
    }

    public ConsoleRunner(String folder) throws FileNotFoundException {
        this(new File(folder));
    }

    public ConsoleRunner(MusicPlayer player) throws FileNotFoundException {
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

    @Override
    public void run() {
        final String appVersion = SysInfo.readVersion();
        final String msg = appVersion != null
                ? "MuPlayer v"+appVersion+" started..."
                : "MuPlayer started...";
        Logger.getLogger(this, msg).rawInfo();
        interpreter.setOn(true);

        ConsoleExecution consoleExecution;

        while (interpreter.isOn()) {
            printHeader();
            consoleExecution = execCommand(scanner.nextLine().trim());
            if (consoleExecution.hasOutput())
                System.out.println(consoleExecution.getOutput());
        }

        System.exit(0);
    }

}
