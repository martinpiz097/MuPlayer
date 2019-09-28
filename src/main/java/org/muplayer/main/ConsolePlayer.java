package org.muplayer.main;

import org.muplayer.audio.Player;
import org.muplayer.audio.Track;
import org.muplayer.audio.interfaces.PlayerListener;
import org.muplayer.system.AppInfo;
import org.muplayer.system.AppKey;
import org.muplayer.system.SysInfo;
import org.muplayer.thread.TaskRunner;
import org.orangelogger.sys.Logger;
import org.orangelogger.sys.SystemUtil;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

public class ConsolePlayer implements Runnable {
    protected volatile Player player;
    protected volatile ConsoleInterpreter interpreter;

    protected volatile File playerFolder;
    protected volatile Scanner scanner;

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

    protected void configListener() {
        player.addPlayerListener(new PlayerListener() {
            @Override
            public void onSongChange(Track newTrack) {
                interpreter.showSongInfo(newTrack);
            }

            @Override
            public void onPlayed(Track track) {

            }

            @Override
            public void onPlaying(Track track) {

            }

            @Override
            public void onResumed(Track track) {

            }

            @Override
            public void onPaused(Track track) {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onStopped(Track track) {

            }

            @Override
            public void onSeeked(Track track) {

            }

            @Override
            public void onShutdown() {

            }
        });
    }

    protected void printHeader() {
        FileOutputStream stdout = SystemUtil.getStdout();
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
                            "Please create a file called config.properties in project path if you want " +
                            "load an folder path automatically and set the property with your music path");
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
