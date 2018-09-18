package org.muplayer.main;

import org.muplayer.audio.Player;
import org.muplayer.audio.Track;
import org.muplayer.system.Logger;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static java.nio.file.StandardOpenOption.WRITE;
import static org.muplayer.main.ConsoleOrder.HELP_MAP;

public class ConsolePlayer extends Thread {
    protected volatile Player player;
    protected volatile CommandInterpreter interpreter;

    protected volatile Scanner scanner;

    protected static final String LINEHEADER = "[MuPlayer]> ";

    public ConsolePlayer(File rootFolder) throws FileNotFoundException {
        player = new Player(rootFolder);
        initInterpreter();
        scanner = new Scanner(System.in);
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
                    Logger.getLogger(this, player.isPlaying()?"Is playing":"Is not playing").rawInfo();
                    break;


                case ConsoleOrder.PLAY:
                    player.play();
                    break;

                case ConsoleOrder.PAUSE:
                    player.pause();
                    break;

                case ConsoleOrder.STOP:
                    player.stopTrack();
                    break;

                case ConsoleOrder.RESUME:
                    player.resumeTrack();
                    break;

                case ConsoleOrder.NEXT:
                    if (cmd.hasOptions()) {
                        Number jumps = cmd.getOptionAsNumber(0);
                        if (jumps == null)
                            Logger.getLogger(this, "Jump value incorrect").rawError();
                        else
                            player.jumpTrack(jumps.intValue());
                    }
                    else
                        player.playNext();
                    break;

                case ConsoleOrder.PREV:
                    player.playPrevious();
                    break;

                case ConsoleOrder.JUMP:
                    if (cmd.hasOptions()) {
                        Number jump = cmd.getOptionAsNumber(0);
                        if (jump == null)
                            Logger.getLogger(this, "Jump value incorrect").rawError();
                        else {
                            player.jumpTrack(jump.intValue());
                            Logger.getLogger(this, "Jumped").rawInfo();
                        }
                    }
                    break;

                case ConsoleOrder.MUTE:
                    player.mute();
                    break;

                case ConsoleOrder.UNMUTE:
                    player.unmute();
                    break;

                case ConsoleOrder.LIST:
                    player.printTracks();
                    break;

                case ConsoleOrder.GETGAIN:
                    Logger.getLogger(this, "Player Volume(0-100): "+player.getGain()).rawInfo();
                    break;

                case ConsoleOrder.SETGAIN:
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
                    player.shutdown();
                    break;

                case ConsoleOrder.SEEK:
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
                    if (cmd.hasOptions()) {
                        String optionParam = cmd.getOptionAt(0);
                        Player.SeekOption option = optionParam.equals("next")? Player.SeekOption.NEXT
                                : (optionParam.equals("prev")? Player.SeekOption.PREV:null);
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
                    else {
                        player.seekFolder(Player.SeekOption.NEXT);
                    }
                    break;
                case ConsoleOrder.GOTOSEC:
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

                case ConsoleOrder.HELP:
                    HashMap<String, String> helpMap = HELP_MAP;

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
                    break;
            }

        };
    }

    protected void startPlayer() {
        try {
            interpreter.interprate(new Command(ConsoleOrder.START));
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Logger.getLogger(this, "MuPlayer started...").rawInfo();
        String cmd;
        startPlayer();

        try {
            while (player.isAlive()) {
                System.out.print(LINEHEADER);
                cmd = scanner.nextLine().trim();
                interpreter.interprate(cmd);
            }
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            Logger.getLogger(this, e.getClass().getSimpleName(), e.getMessage()).error();
        }
    }

    public static void main(String[] args) {
        try {
            //new ConsolePlayer(args[0]).start();
            new ConsolePlayer("/home/martin/Escritorio/Archivos/Música").start();
        } catch (Exception e) {
            Logger.getLogger(ConsolePlayer.class, e.getClass().getSimpleName(), e.getMessage()).error();
        }
    }

}
