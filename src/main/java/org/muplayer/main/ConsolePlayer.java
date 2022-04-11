package org.muplayer.main;

import org.muplayer.audio.Player;
import org.muplayer.system.AppInfo;
import org.muplayer.system.AppKey;
import org.muplayer.system.SysInfo;
import org.muplayer.thread.TaskRunner;
import org.orangelogger.sys.Logger;
import org.orangelogger.sys.SystemUtil;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class ConsolePlayer implements Runnable {
    protected final Player player;
    protected final ConsoleInterpreter interpreter;

    protected final File playerFolder;
    protected final Scanner scanner;

    protected static final String LINEHEADER = "[MuPlayer]> ";

    public ConsolePlayer(File rootFolder) throws FileNotFoundException {
        player = new Player(rootFolder);
        interpreter = new ConsoleInterpreter(player);
        this.playerFolder = rootFolder;
        scanner = new Scanner(System.in);
    }

    public ConsolePlayer(String folder) throws FileNotFoundException {
        this(new File(folder));
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

    public Player getPlayer() {
        return player;
    }

    public void execCommand(String strCmd) {
        try {
            interpreter.preInterprate(strCmd);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (SysInfo.VERSION == null) {
            Logger.getLogger(this, "MuPlayer started...").rawInfo();
        }
        else {
            Logger.getLogger(this, "MuPlayer v"+SysInfo.VERSION+" started...").rawInfo();
        }
        interpreter.setOn(true);

        while (interpreter.isOn()) {
            printHeader();
            execCommand(scanner.nextLine().trim());
        }

        System.exit(0);
    }

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                final String defaultRootPath = AppInfo.getInstance().get(AppKey.DEFAULT_ROOT_FOLDER);
                if (defaultRootPath == null) {
                    throw new NullPointerException("Property 'root_folder' must be configured.\n" +
                            "If you want to load a folder path automatically, create a file called config.properties in " +
                            "the path of the jar file and set the root_folder property indicating the path of your music folder");
                }
                else {
                    TaskRunner.execute(new ConsolePlayer(defaultRootPath));
                }
            }
            else
                TaskRunner.execute(new ConsolePlayer(args[0]));
        } catch (Exception e) {
            e.printStackTrace();
            //Logger.getLogger(ConsolePlayer.class, e.getClass().getSimpleName(), e.getMessage()).error();
        }
    }

}
