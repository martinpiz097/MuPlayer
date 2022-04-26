package org.muplayer.main;

import lombok.Getter;
import lombok.Setter;
import org.muplayer.audio.Player;
import org.muplayer.console.ConsoleInterpreter;
import org.muplayer.properties.AppConfig;
import org.muplayer.properties.AppInfoKeys;
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

public class ConsolePlayer extends Thread {
    @Getter @Setter
    protected Player player;
    protected final ConsoleInterpreter interpreter;
    protected final Scanner scanner;

    protected static final String LINEHEADER = "[MuPlayer]> ";

    public ConsolePlayer() throws FileNotFoundException {
        player = new Player((File) null);
        interpreter = new ConsoleInterpreter(player);
        scanner = new Scanner(System.in);
    }

    public ConsolePlayer(File rootFolder) throws FileNotFoundException {
        player = new Player(rootFolder);
        interpreter = new ConsoleInterpreter(player);
        scanner = new Scanner(System.in);
    }

    public ConsolePlayer(String folder) throws FileNotFoundException {
        this(new File(folder));
    }

    public ConsolePlayer(Player player) throws FileNotFoundException {
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

    public void execCommand(String strCmd) {
        try {
            interpreter.interprateCommand(strCmd);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        final String appVersion = SysInfo.readVersion();
        final String msg = appVersion != null
                ? "MuPlayer v"+appVersion+" started..."
                : "MuPlayer started...";
        Logger.getLogger(this, msg).rawInfo();
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
                final String defaultRootPath = AppConfig.getInstance().get(AppInfoKeys.DEFAULT_ROOT_FOLDER);
                if (defaultRootPath == null) {
                    throw new NullPointerException("Property 'root_folder' must be configured.\n" +
                            "If you want to load a folder path automatically, create a file called config.properties in " +
                            "the path of the jar file and set the root_folder property indicating the path of your music folder");
                }
                else
                    TaskRunner.execute(new ConsolePlayer(defaultRootPath));
            }
            else
                TaskRunner.execute(new ConsolePlayer(args[0]));
        } catch (Exception e) {
            e.printStackTrace();
            //Logger.getLogger(ConsolePlayer.class, e.getClass().getSimpleName(), e.getMessage()).error();
        }
    }

}
