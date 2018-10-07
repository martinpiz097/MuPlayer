package org.muplayer.main;

import org.muplayer.audio.Player;
import org.muplayer.audio.SeekOption;
import org.muplayer.audio.Track;
import org.muplayer.system.SysInfo;
import org.orangelogger.sys.ConsoleColor;
import org.orangelogger.sys.Logger;
import org.orangelogger.sys.SystemUtil;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import static java.nio.file.StandardOpenOption.WRITE;
import static org.muplayer.main.ConsoleOrder.HELP_MAP;

public class ConsolePlayer extends Thread {
    protected volatile Player player;
    protected volatile CommandInterpreter interpreter;

    protected volatile File playerFolder;
    protected volatile Scanner scanner;
    protected boolean on;

    protected static final String LINEHEADER = "[MuPlayer]> ";

    public ConsolePlayer(File rootFolder) throws FileNotFoundException {
        player = new Player(rootFolder);
        initInterpreter();
        this.playerFolder = rootFolder;
        scanner = new Scanner(System.in);
        on = false;
        setName("ConsolePlayer");
    }

    public ConsolePlayer(String folder) throws FileNotFoundException {
        this(new File(folder));
    }

    protected void initInterpreter() {
        interpreter = cmd -> {
            final String cmdOrder = cmd.getOrder();
            Track current;

            switch (cmdOrder) {
                case ConsoleOrder.START:
                    if (player == null)
                        player = new Player(playerFolder);
                    if (player.isAlive() && cmd.hasOptions()) {
                        File musicFolder = new File(cmd.getOptionAt(0));
                        if (musicFolder.exists()) {
                            Player newPlayer = new Player(musicFolder);
                            player.shutdown();
                            newPlayer.start();
                            player = newPlayer;
                        }
                        else
                            Logger.getLogger(this, "Folder not exists").rawError();
                    }
                    else if(!player.isAlive())
                        player.start();

                    break;
                case ConsoleOrder.ISSTARTED:
                    if (player != null)
                        Logger.getLogger(this, player.isPlaying()?"Is playing":"Is not playing").rawInfo();
                    break;


                case ConsoleOrder.PLAY:
                    if (player != null)
                        if (cmd.hasOptions()) {
                            Number playIndex = cmd.getOptionAsNumber(0);
                            if (playIndex != null &&
                                    playIndex.intValue() > 0 && playIndex.intValue() <= player.getSongsCount())
                                player.play(playIndex.intValue()-1);
                        }
                        else
                            player.play();
                    break;

                case ConsoleOrder.PAUSE:
                    if (player != null)
                        player.pause();
                    break;

                case ConsoleOrder.STOP:
                    if (player != null)
                        player.stopTrack();
                    break;

                case ConsoleOrder.RESUME:
                    if (player != null)
                        player.resumeTrack();
                    break;

                case ConsoleOrder.NEXT:
                    if (player != null)
                        if (cmd.hasOptions()) {
                            Number jumps = cmd.getOptionAsNumber(0);
                            if (jumps == null)
                                Logger.getLogger(this, "Jump value incorrect").rawError();
                            else
                                player.jumpTrack(jumps.intValue(), SeekOption.NEXT);
                        }
                        else
                            player.playNext();
                    break;

                case ConsoleOrder.PREV:
                    if (player != null)
                        if (cmd.hasOptions()) {
                            Number jumps = cmd.getOptionAsNumber(0);
                            if (jumps == null)
                                Logger.getLogger(this, "Jump value incorrect").rawError();
                            else
                                player.jumpTrack(jumps.intValue(), SeekOption.PREV);
                        }
                        else
                            player.playPrevious();
                    break;

                /*case ConsoleOrder.JUMP:
                    if (cmd.hasOptions()) {
                        Number jump = cmd.getOptionAsNumber(0);
                        if (jump == null)
                            Logger.getLogger(this, "Jump value incorrect").rawError();
                        else {
                            player.jumpTrack(jump.intValue(), SeekOption.NEXT);
                            Logger.getLogger(this, "Jumped").rawInfo();
                        }
                    }
                    break;*/

                case ConsoleOrder.MUTE:
                    if (player != null)
                        player.mute();
                    break;

                case ConsoleOrder.UNMUTE:
                    if (player != null)
                        player.unmute();
                    break;

                case ConsoleOrder.LIST1:
                    if (player != null)
                        player.printTracks();
                    break;

                case ConsoleOrder.LIST2:
                    if (player != null)
                        player.printTracks();
                    break;

                case ConsoleOrder.LISTCURRENTFOLDER:
                    if (player != null)
                        player.printFolderTracks();
                    break;

                case ConsoleOrder.LISTFOLDERS:
                    if (player != null)
                        player.printFolders();
                    break;

                case ConsoleOrder.GETGAIN:
                    if (player != null)
                        Logger.getLogger(this, "Player Volume(0-100): "+player.getGain()).rawInfo();
                    break;

                case ConsoleOrder.SETGAIN:
                    if (player != null)
                        if (cmd.hasOptions()) {
                            Number volume = cmd.getOptionAsNumber(0);
                            if (volume == null)
                                Logger.getLogger(this, "Volume value incorrect").rawError();
                            else {
                                player.setGain(volume.floatValue());
                                Logger.getLogger(this, "Volume value changed").rawInfo();
                            }
                        }
                    break;
                case ConsoleOrder.SHUTDOWN:
                    if (player != null) {
                        player.shutdown();
                        player = null;
                    }
                    break;

                case ConsoleOrder.EXIT:
                    if (player != null) {
                        player.shutdown();
                        player = null;
                    }
                    on = false;
                    break;

                case ConsoleOrder.QUIT:
                    if (player != null) {
                        player.shutdown();
                        player = null;
                    }
                    on = false;
                    break;

                case ConsoleOrder.SEEK:
                    if (player != null)
                        if (cmd.hasOptions()) {
                            Number seekSec = cmd.getOptionAsNumber(0);
                            if (seekSec == null)
                                Logger.getLogger(this, "Seek value incorrect").rawError();
                            else {
                                player.seek(seekSec.doubleValue());
                                Logger.getLogger(this, "Seeked").rawInfo();
                            }
                        }
                    break;
                case ConsoleOrder.SEEKFLD:
                    if (player != null)
                        if (cmd.hasOptions()) {
                            String optionParam = cmd.getOptionAt(0);
                            SeekOption option = optionParam.equals("next") ? SeekOption.NEXT
                                    : (optionParam.equals("prev") ? SeekOption.PREV : null);
                            Number jumps = cmd.getOptionAsNumber(1);

                            if (cmd.getOptionsCount() > 1 && jumps == null)
                                Logger.getLogger(this, "Seek value incorrect").rawError();
                            else {
                                if (jumps == null && option == null)
                                    Logger.getLogger(this, "Option value incorrect").rawError();
                                else if (jumps == null)
                                    player.seekFolder(option);
                                else if (jumps.intValue() < 0)
                                    Logger.getLogger(this, "Jumps value incorrect").rawError();
                                else {
                                    player.seekFolder(option, jumps.intValue());
                                    Logger.getLogger(this, "Seeked").rawInfo();
                                }
                            }

                        }
                        else
                            player.seekFolder(SeekOption.NEXT);
                    break;

                case ConsoleOrder.RELOAD:
                    if (player != null)
                        player.reloadTracks();
                    break;
                case ConsoleOrder.GOTOSEC:
                    if (player != null)
                        if (cmd.hasOptions()) {
                            Number gotoSec = cmd.getOptionAsNumber(0);
                            if (gotoSec == null)
                                Logger.getLogger(this, "Go to value incorrect").rawError();
                            else {
                                player.seek(gotoSec.doubleValue());
                                Logger.getLogger(this, "Right Goto command").rawInfo();
                            }
                        }
                    break;
                case ConsoleOrder.SOUNDCOUNT:
                    if (player != null)
                        Logger.getLogger(this, player.getSongsCount()).info();
                    break;
                case ConsoleOrder.DURATION:
                    if (player != null)
                        Logger.getLogger(this, player.getCurrent().getFormattedDuration()).info();
                    break;
                case ConsoleOrder.GETCOVER:
                    current = player.getCurrent();
                    if (current == null)
                        Logger.getLogger(this, "Current track unavailable").rawError();
                    else if (!current.hasCover())
                        Logger.getLogger(this, "Current song don't have cover").rawError();
                    else if (cmd.hasOptions()) {
                        File folderPath = new File(cmd.getOptionAt(0));
                        if (!folderPath.exists())
                            folderPath = player.getRootFolder();
                        File fileCover = new File(folderPath, "cover-"+current.getTitle()+".png");
                        fileCover.createNewFile();
                        Files.write(fileCover.toPath(), current.getCoverData(), WRITE);
                        Logger.getLogger(this, "Created cover with name "+fileCover.getName()).rawInfo();
                    }
                    else {
                        Logger.getLogger(this, "Cover path not defined").rawError();
                    }
                    break;

                case ConsoleOrder.GETINFO:
                    current = player.getCurrent();
                    if (current == null)
                        Logger.getLogger(this, "Current track unavailable").rawError();
                    else
                        Logger.getLogger(this, current.getSongInfo()).rawInfo();
                    break;

                case ConsoleOrder.GETPROGRESS:
                    current = player.getCurrent();
                    if (current == null)
                        Logger.getLogger(this, "Current track unavailable").rawError();
                    else
                        Logger.getLogger(this, current.getProgress()).rawInfo();
                    break;

                case ConsoleOrder.CLEAR1:
                    clearConsole();
                    break;

                case ConsoleOrder.CLEAR2:
                    clearConsole();
                    break;

                case ConsoleOrder.FORMAT:
                    current = player.getCurrent();
                    if (current == null)
                        Logger.getLogger(this, "Current track unavailable").rawError();
                    else {
                        String className = current.getClass().getSimpleName();
                        Logger.getLogger(
                                this,
                                className.substring(0, className.length()-5).toLowerCase()).rawInfo();

                    }
                    break;

                case ConsoleOrder.HELP1:
                    printHelp();
                    break;

                case ConsoleOrder.HELP2:
                    printHelp();
                    break;

                default:
                    Logger.getLogger(this, "Comando desconocido, inserte el comando \"h\" o \"help\"\n" +
                            "para desplegar el menú de ayuda.").rawWarning();
                    break;
            }

        };
    }

    protected void printStreamOut(InputStream cmdStream) throws IOException {
        int read;
        FileOutputStream stdout = SystemUtil.getStdout();
        while ((read = cmdStream.read()) != -1)
            stdout.write(read);
    }

    protected void clearConsole() throws IOException {
        Process process;
        if (SysInfo.ISUNIX)
            process = Runtime.getRuntime().exec("clear");
        else
            process = Runtime.getRuntime().exec ("cls");
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (process.exitValue() == 0)
            printStreamOut(process.getInputStream());
        else
            printStreamOut(process.getErrorStream());
    }

    protected void printHelp() {
        Map<String, String> helpMap = HELP_MAP;

        Iterator<Map.Entry<String, String>> it = helpMap.entrySet().iterator();
        Map.Entry<String, String> entry;
        StringBuilder sbHelp = new StringBuilder();
        int count = 1;
        while (it.hasNext()) {
            entry = it.next();
            sbHelp.append(count).append(") ")
                    .append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append('\n');
            count++;
        }
        Logger.getLogger(this, "---------").rawInfo();
        Logger.getLogger(this, "Help Info").rawInfo();
        Logger.getLogger(this, "---------").rawInfo();
        Logger.getLogger(this, sbHelp.toString()).rawInfo();
    }

    /*protected void startPlayer() {
        try {
            interpreter.interprate(new Command(ConsoleOrder.START));
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }*/

    protected void printHeader() throws IOException {
        FileOutputStream stdout = SystemUtil.getStdout();
        stdout.write(Logger.getLogger(this, LINEHEADER)
                .getColoredMsg(ConsoleColor.ANSI_CYAN).getBytes());
        stdout.flush();
    }

    @Override
    public void run() {
        Logger.getLogger(this, "MuPlayer started...").rawInfo();
        String cmd;
        //startPlayer();
        on = true;

        try {
            while (on) {
                printHeader();
                cmd = scanner.nextLine().trim();
                interpreter.interprate(cmd);
            }
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            Logger.getLogger(this, e.getClass().getSimpleName(), e.getMessage()).error();
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length == 0)
                new ConsolePlayer("/home/martin/Escritorio/Archivos/Música").start();
            else
                new ConsolePlayer(args[0]).start();
        } catch (Exception e) {
            Logger.getLogger(ConsolePlayer.class, e.getClass().getSimpleName(), e.getMessage()).error();
        }
    }

}
