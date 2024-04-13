package org.muplayer.console;

import org.muplayer.audio.player.MusicPlayer;
import org.muplayer.audio.track.Track;
import org.muplayer.audio.player.Player;
import org.muplayer.console.runner.ConsoleRunner;
import org.muplayer.console.runner.LocalRunner;
import org.muplayer.console.runner.RunnerMode;
import org.muplayer.model.Album;
import org.muplayer.model.Artist;
import org.muplayer.model.SeekOption;
import org.muplayer.console.runner.DaemonRunner;
import org.muplayer.properties.help.HelpInfo;
import org.muplayer.system.*;
import org.muplayer.thread.TaskRunner;
import org.muplayer.util.TrackUtil;
import org.orangelogger.sys.Logger;
import org.orangelogger.sys.SystemUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.WRITE;
import static org.muplayer.console.OutputType.*;
import static org.muplayer.system.GlobalVar.RUNNER;

public class ConsoleInterpreter implements CommandInterpreter {
    private Player player;
    private final File playerFolder;
    private boolean on;

    private final HelpInfo helpInfo;

    private static final String CMD_DIVISOR = " && ";

    public ConsoleInterpreter(Player player) {
        this.player = player;
        this.playerFolder = player.getRootFolder();
        helpInfo = HelpInfo.getInstance();
    }

    private boolean isPlayerOn() {
        return player != null &&
                player.isAlive();
    }

    private void execSysCommand(String cmd) {
        Process process;
        try {
            process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            if (process.exitValue() == 0)
                printStreamOut(process.getInputStream());
            else
                printStreamOut(process.getErrorStream());
        } catch (IOException | InterruptedException e) {
            Logger.getLogger(this, e.getMessage()).error();
        }
    }

    private void printTracks(ConsoleExecution execution) {
        final File rootFolder = player.getRootFolder();
        final List<Track> listTracks = player.getTracks();
        final Track current = player.getCurrent();

        execution.appendOutput("------------------------------", INFO);
        if (rootFolder == null)
            execution.appendOutput("Music in folder", INFO);
        else
            execution.appendOutput("Music in folder "+rootFolder.getName(), INFO);
        execution.appendOutput("------------------------------", INFO);

        if (rootFolder != null) {
            Track track;
            File fileTrack;
            for (int i = 0; i < player.getSongsCount(); i++) {
                track = listTracks.get(i);
                fileTrack = track.getDataSourceAsFile();
                if (current != null && fileTrack.getPath().equals(((File)current.getDataSourceAsFile()).getPath()))
                    execution.appendOutput("Track "+(i+1)+": "
                            +fileTrack.getName(), WARNING);
                else
                    execution.appendOutput("Track "+(i+1)+": "
                            +fileTrack.getName(), INFO);
            }
            execution.appendOutput("------------------------------", INFO);
        }
    }

    private void printDetailedTracks(ConsoleExecution execution) {
        final File rootFolder = player.getRootFolder();
        final List<Track> listTracks = player.getTracks();
        //final List<String> listFolderPaths = player.getListFolders().stream()
        //        .map(File::getPath).collect(Collectors.toList());
        final Track current = player.getCurrent();

        execution.appendOutput("------------------------------", INFO);
        if (rootFolder == null)
            execution.appendOutput("Music in folder", INFO);
        else
            execution.appendOutput("Music in folder "+rootFolder.getName(), INFO);
        execution.appendOutput("------------------------------", INFO);

        if (rootFolder != null) {
            File trackFile;
            File trackFolder, prevTrackFolder = null;
            for (int i = 0; i < player.getSongsCount(); i++) {
                trackFile = listTracks.get(i).getDataSourceAsFile();
                trackFolder = trackFile.getParentFile();
                if (prevTrackFolder == null || !trackFolder.getPath().equals(prevTrackFolder.getPath()))
                    execution.appendOutput((prevTrackFolder != null
                            ? "----------------------------------------------------------------------" +
                            "\n\n----------------------------------------------------------------------" +
                            "\nFolder: "
                            : "----------------------------------------------------------------------\n"
                            + "Folder: ")+trackFolder.getName(), INFO);
                if (current != null && trackFile.getPath().equals(current.getDataSourceAsFile().getPath()))
                    execution.appendOutput("\tTrack "+(i+1)+": "
                            +trackFile.getName(), WARNING);
                else
                    execution.appendOutput("\tTrack "+(i+1)+": "
                            +trackFile.getName(), INFO);

                prevTrackFolder = trackFile.getParentFile();
            }
            execution.appendOutput("------------------------------", INFO);
        }
    }

    private synchronized void printFolderTracks(ConsoleExecution execution) {
        final List<Track> listTracks = player.getTracks();
        final Track current = player.getCurrent();
        final int songsCount = player.getSongsCount();

        File parentFolder = current == null ? null : current.getDataSourceAsFile().getParentFile();

        execution.appendOutput("------------------------------", INFO);
        if (parentFolder == null)
            execution.appendOutput("Music in current folder", INFO);
        else
            execution.appendOutput("Music in folder "+parentFolder.getName(), INFO);
        execution.appendOutput("------------------------------", INFO);

        if (parentFolder != null) {
            File fileTrack;
            File currentFile = current.getDataSourceAsFile();

            for (int i = 0; i < songsCount; i++) {
                fileTrack = listTracks.get(i).getDataSourceAsFile();
                if (fileTrack.getParentFile().equals(parentFolder)) {
                    if (fileTrack.getPath().equals(currentFile.getPath()))
                        execution.appendOutput("Track "+(i+1)+": "
                                +fileTrack.getName(), WARNING);
                    else
                        execution.appendOutput("Track "+(i+1)+": "
                                +fileTrack.getName(), INFO);
                }
            }
            execution.appendOutput("------------------------------", INFO);
        }
    }

    private synchronized void printFolderTracks(ConsoleExecution execution, int index) {
        final File folder = player.getListFolders().get(index-1);
        final File currentFile = player.getCurrent().getDataSourceAsFile();

        execution.appendOutput("------------------------------", INFO);

        if (folder != null) {
            execution.appendOutput("Music in folder "+folder.getName(), INFO);
            execution.appendOutput("------------------------------", INFO);

            final AtomicInteger counter = new AtomicInteger(1);
            player.getTracks().stream().filter(track->track.getDataSourceAsFile().getParent()
                    .equals(folder.getPath())).forEach(track->{
                        File fileTrack = track.getDataSourceAsFile();
                        if (fileTrack.getParentFile().equals(folder)) {
                            if (fileTrack.getPath().equals(currentFile.getPath()))
                                execution.appendOutput("Track "+(counter.getAndIncrement())+": "
                                        +fileTrack.getName(), WARNING);
                            else
                                execution.appendOutput("Track "+(counter.getAndIncrement())+": "
                                        +fileTrack.getName(), INFO);
                        }
            });
            execution.appendOutput("------------------------------", INFO);
        }
    }

    private synchronized void printFolders(ConsoleExecution execution) {
        final File rootFolder = player.getRootFolder();
        final List<String> listFolderPaths = player.getListFolders().stream().map(
                File::getPath).collect(Collectors.toList());
        final Track current = player.getCurrent();

        execution.appendOutput("------------------------------", INFO);
        if (rootFolder == null)
            execution.appendOutput("Folders", INFO);
        else
            execution.appendOutput("Folders in "+rootFolder.getName(), INFO);
        execution.appendOutput("------------------------------", INFO);

        if (current == null)
            return;

        File currentTrackFile = current.getDataSourceAsFile();
        File folder;

        for (int i = 0; i < player.getFoldersCount(); i++) {
            folder = new File(listFolderPaths.get(i));
            if (folder.getPath().equals(currentTrackFile.getParentFile().getPath())) {
                execution.appendOutput("Folder "+(i+1)+": "
                        +folder.getName(), WARNING);
            }
            else {
                execution.appendOutput("Folder "+(i+1)+": "
                        +folder.getName(), INFO);
            }

        }
        execution.appendOutput("------------------------------", INFO);
    }

    protected void printStreamOut(InputStream cmdStream) throws IOException {
        int read;
        FileOutputStream stdout = SystemUtil.getStdout();
        while ((read = cmdStream.read()) != -1)
            stdout.write(read);
    }

    protected void clearConsole() throws IOException {
        Process process;
        if (SysInfo.IS_UNIX)
            process = Runtime.getRuntime().exec("clear");
        else
            process = Runtime.getRuntime().exec("cls");
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

    protected void printHelp(ConsoleExecution execution) {
        final Set<String> propertyNames = helpInfo.getPropertyNames();
        final Iterator<String> it = propertyNames.iterator();
        final StringBuilder sbHelp = new StringBuilder();

        String key;
        int count = 1;
        while (it.hasNext()) {
            key = it.next();
            sbHelp.append(count++).append(") ")
                    .append(key)
                    .append(": ")
                    .append(helpInfo.getProperty(key))
                    .append('\n');
        }
        execution.appendOutput("---------", WARNING);
        execution.appendOutput("Help Info", WARNING);
        execution.appendOutput("---------", WARNING);
        execution.appendOutput(sbHelp.toString(), WARNING);
    }

    public void showSongInfo(Track track, ConsoleExecution execution) {
        if (track == null)
            execution.appendOutput("Current track unavailable", ERROR);
        else
            execution.appendOutput(TrackUtil.getSongInfo(track), WARNING);
    }

    public ConsoleExecution executeCommand(String cmd) throws Exception {
        if (cmd.contains(CMD_DIVISOR)) {
            final String[] cmdSplit = cmd.split(CMD_DIVISOR);
            ConsoleExecution consoleExecution = new ConsoleExecution();

            final List<String> listExec = new ArrayList<>();
            ConsoleExecution exec;

            for (int i = 0; i < cmdSplit.length; i++) {
                exec = executeCommand(cmdSplit[i].trim());
                if (exec != null)
                    listExec.add(exec.getOutputMsg());
            }
            consoleExecution.setCmd(cmd);
            consoleExecution.appendOutput(listExec.get(listExec.size()-1), "");
            return consoleExecution;
        }
        else
            return executeCommand(new Command(cmd));
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    @Override
    public ConsoleExecution executeCommand(Command cmd) throws Exception {
        final String cmdOrder = cmd.getOrder();
        Track current;

        final ConsoleExecution execution = new ConsoleExecution();
        execution.setCmd(cmd.toString());
        // imprimir output de este objeto no mas

        switch (cmdOrder) {
            case ConsoleOrder.START:
                if (player == null)
                    player = new MusicPlayer(playerFolder);
                if (player.isAlive() && cmd.hasOptions()) {
                    File musicFolder = new File(cmd.getOptionAt(0));
                    if (musicFolder.exists()) {
                        Player newMusicPlayer = new MusicPlayer(musicFolder);
                        player.shutdown();
                        newMusicPlayer.start();
                        player = newMusicPlayer;
                    } else
                        execution.appendOutput("Folder not exists", ERROR);
                } else if (!player.isAlive())
                    player.start();
                if (player.getCurrent() != null) {
                    showSongInfo(player.getCurrent(), execution);
                }
                break;
            case ConsoleOrder.ISSTARTED:
                execution.appendOutput(isPlayerOn() ? "Is playing" : "Is not playing", WARNING);
                break;

            case ConsoleOrder.PLAY:
                if (isPlayerOn())
                    if (cmd.hasOptions()) {
                        Number playIndex = cmd.getOptionAsNumber(0);
                        if (playIndex != null &&
                                playIndex.intValue() > 0 && playIndex.intValue() <= player.getSongsCount())
                            player.play(playIndex.intValue() - 1);
                        showSongInfo(player.getCurrent(), execution);
                    } else
                        player.play();
                break;

            case ConsoleOrder.PAUSE:
                if (isPlayerOn())
                    player.pause();
                break;

            case ConsoleOrder.STOP:
                if (isPlayerOn()) {
                    try {
                        player.stopTrack();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case ConsoleOrder.RESUME:
                if (isPlayerOn())
                    player.resumeTrack();
                break;

            case ConsoleOrder.NEXT:
                if (isPlayerOn()) {
                    if (cmd.hasOptions()) {
                        Number jumps = cmd.getOptionAsNumber(0);
                        if (jumps == null)
                            execution.appendOutput("Jump value incorrect", ERROR);
                        else
                            player.jumpTrack(jumps.intValue(), SeekOption.NEXT);
                    }
                    else
                        player.playNext();
                }
                showSongInfo(player.getCurrent(), execution);
                break;

            case ConsoleOrder.PREV:
                if (isPlayerOn()) {
                    if (cmd.hasOptions()) {
                        Number jumps = cmd.getOptionAsNumber(0);
                        if (jumps == null)
                            execution.appendOutput("Jump value incorrect", ERROR);
                        else
                            player.jumpTrack(jumps.intValue(), SeekOption.PREV);
                    } else
                        player.playPrevious();
                    showSongInfo(player.getCurrent(), execution);
                }
                break;

            case ConsoleOrder.MUTE:
                if (isPlayerOn())
                    player.mute();
                break;

            case ConsoleOrder.UNMUTE:
                if (isPlayerOn())
                    player.unMute();
                break;

            /*case ConsoleOrder.MUTESYSVOL:
                if (isPlayerOn())
                    player.muteSystemVolume();
                break;

            case ConsoleOrder.UNMUTESYSVOL:
                if (isPlayerOn())
                    player.unmuteSystemVolume();
                break;*/

            case ConsoleOrder.LIST1:
            case ConsoleOrder.LIST2:
                if (player != null)
                    printTracks(execution);
                break;

            case ConsoleOrder.LISTCURRENTFOLDER:
                if (isPlayerOn())
                    printFolderTracks(execution);
                break;

            case ConsoleOrder.LISTFOLDERS:
                if (isPlayerOn()) {
                    if (cmd.hasOptions()) {
                        try {
                            Number index = cmd.getOptionAsNumber(0);
                            printFolderTracks(execution, index.intValue());
                        } catch (NumberFormatException e) {
                        }
                    }
                    else
                        printFolders(execution);
                }
                break;

            case ConsoleOrder.LISTDETAILED:
                if (isPlayerOn())
                    printDetailedTracks(execution);
                break;

            case ConsoleOrder.GETGAIN:
                if (player != null)
                    execution.appendOutput("Player Volume(0-100): " + player.getVolume(), WARNING);
                break;

            case ConsoleOrder.SETGAIN:
                if (player != null)
                    if (cmd.hasOptions()) {
                        Number volume = cmd.getOptionAsNumber(0);
                        if (volume == null)
                            execution.appendOutput("Volume value incorrect", ERROR);
                        else {
                            player.setVolume(volume.floatValue());
                            execution.appendOutput("Volume value changed", WARNING);
                        }
                    }
                break;
            case ConsoleOrder.GETSYSVOL:
                if (player != null)
                    execution.appendOutput("Player Volume(0-100): " + player.getSystemVolume(), WARNING);
                break;

            case ConsoleOrder.SETSYSVOL:
                if (player != null)
                    if (cmd.hasOptions()) {
                        Number volume = cmd.getOptionAsNumber(0);
                        if (volume == null)
                            execution.appendOutput("Volume value incorrect", ERROR);
                        else {
                            player.setSystemVolume(volume.floatValue());
                            execution.appendOutput("Volume value changed", WARNING);
                        }
                    }
                break;

            case ConsoleOrder.SHUTDOWN:
            case ConsoleOrder.EXIT:
            case ConsoleOrder.QUIT:
                if (isPlayerOn()) {
                    player.shutdown();
                    player = null;
                }
                on = false;
                break;

            case ConsoleOrder.SEEK:
                if (isPlayerOn())
                    if (cmd.hasOptions()) {
                        Number seekSec = cmd.getOptionAsNumber(0);
                        if (seekSec == null)
                            execution.appendOutput("Seek value incorrect", ERROR);
                        else {
                            player.seek(seekSec.doubleValue());
                        }
                    }
                break;
            case ConsoleOrder.SEEKFLD:
                if (isPlayerOn()) {
                    if (cmd.hasOptions()) {
                        String optionParam = cmd.getOptionAt(0);
                        SeekOption option = optionParam.equals("next") ? SeekOption.NEXT
                                : (optionParam.equals("prev") ? SeekOption.PREV : null);
                        Number jumps = cmd.getOptionAsNumber(1);

                        if (cmd.getOptionsCount() > 1 && jumps == null)
                            execution.appendOutput("Seek value incorrect", ERROR);
                        else {
                            if (jumps == null && option == null)
                                execution.appendOutput("Option value incorrect", ERROR);
                            else if (jumps == null)
                                player.seekFolder(option);
                            else if (jumps.intValue() < 0)
                                execution.appendOutput("Jumps value incorrect", ERROR);
                            else
                                player.seekFolder(option, jumps.intValue());
                        }

                    } else
                        player.seekFolder(SeekOption.NEXT);
                    showSongInfo(player.getCurrent(), execution);
                }
                break;

            case ConsoleOrder.RELOAD:
                if (player != null)
                    player.reload();
                break;
            case ConsoleOrder.GOTOSEC:
                if (player != null)
                    if (cmd.hasOptions()) {
                        Number gotoSec = cmd.getOptionAsNumber(0);
                        if (gotoSec == null)
                            execution.appendOutput("Go to value incorrect", ERROR);
                        else
                            player.gotoSecond(gotoSec.doubleValue());
                    }
                break;
            case ConsoleOrder.SOUNDCOUNT:
                final int count = player.getSongsCount();
                if (player != null)
                    execution.appendOutput(player.getSongsCount(), INFO);
                break;
            case ConsoleOrder.DURATION:
                if (player != null)
                    execution.appendOutput(player.getCurrent().getFormattedDuration(), INFO);
                break;
            case ConsoleOrder.GETCOVER:
                current = player.getCurrent();
                if (current == null)
                    execution.appendOutput("Current track unavailable", ERROR);
                else if (!current.hasCover())
                    execution.appendOutput("Current song don't have cover", ERROR);
                else if (cmd.hasOptions()) {
                    File folderPath = new File(cmd.getOptionAt(0));
                    if (!folderPath.exists())
                        folderPath = player.getRootFolder();
                    File fileCover = new File(folderPath, "cover-" + current.getTitle() + ".png");
                    fileCover.createNewFile();
                    Files.write(fileCover.toPath(), current.getCoverData(), WRITE);
                    execution.appendOutput("Created cover with name " + fileCover.getName(), WARNING);
                } else
                    execution.appendOutput("Cover path not defined", ERROR);
                break;

            case ConsoleOrder.GETINFO:
                if (player.hasSounds() && player.getCurrent() != null)
                    showSongInfo(player.getCurrent(), execution);
                else
                    execution.appendOutput("No song available", WARNING);
                break;

            case ConsoleOrder.GETPROGRESS:
                current = player.getCurrent();
                if (current == null) {
                    execution.appendOutput("Current track unavailable", ERROR);
                }
                else {
                    final String formattedProgress = current.getFormattedProgress();;
                    final String formattedDuration = current.getFormattedDuration();
                    execution.appendOutput(formattedProgress+"/"+formattedDuration, WARNING);
                }
                break;

            case ConsoleOrder.CLEAR1:
            case ConsoleOrder.CLEAR2:
                clearConsole();
                break;

            case ConsoleOrder.FORMAT:
                current = player.getCurrent();
                if (current == null)
                    execution.appendOutput("Current track unavailable", ERROR);
                else {
                    final String className = current.getClass().getSimpleName();
                    execution.appendOutput(className.substring(0, className.length() - 5).toLowerCase(), WARNING);
                }
                break;

            case ConsoleOrder.TITLE:
                current = player.getCurrent();
                if (current == null)
                    execution.appendOutput("Current track unavailable", ERROR);
                else
                    execution.appendOutput(current.getTitle(), WARNING);
                break;

            case ConsoleOrder.NAME:
                current = player.getCurrent();
                if (current == null)
                    execution.appendOutput("Current track unavailable", ERROR);
                else
                    execution.appendOutput(current.getDataSourceAsFile().getName(), WARNING);
                break;

            case ConsoleOrder.HELP1:
            case ConsoleOrder.HELP2:
                printHelp(execution);
                break;

            case ConsoleOrder.SYSTEM1:
            case ConsoleOrder.SYSTEM2:
                if (cmd.hasOptions())
                    execSysCommand(cmd.getOptionsAsString());
                break;

            case ConsoleOrder.SHOW_NEXT:
                if (isPlayerOn())
                    execution.appendOutput(TrackUtil.getSongInfo(player.getNext()), WARNING);
                break;

            case ConsoleOrder.SHOW_PREV:
                if (isPlayerOn())
                    execution.appendOutput(TrackUtil.getSongInfo(player.getPrevious()), WARNING);
                break;

            case ConsoleOrder.PLAY_FOLDER:
                if (isPlayerOn()) {
                    if (cmd.hasOptions()) {
                        final Number fldIndex = cmd.getOptionAsNumber(0);
                        if (fldIndex != null && fldIndex.intValue() > 0) {
                            player.playFolder(fldIndex.intValue()-1);
                            showSongInfo(player.getCurrent(), execution);
                        }
                    }
                }
                break;

            case ConsoleOrder.LOAD:
                if (cmd.hasOptions()) {
                    File folder = new File(cmd.getOptionAt(0));
                    if (folder.exists()) {
                        if (folder.isDirectory()) {
                            if (folder.list() == null)
                                execution.appendOutput("Folder is empty", ERROR);
                            else {
                                final Player newMusicPlayer = new MusicPlayer(folder);
                                newMusicPlayer.start();
                                if (isPlayerOn())
                                    player.shutdown();
                                player = newMusicPlayer;
                            }
                        }
                        else
                            execution.appendOutput("Path not be a directory", ERROR);
                    }
                    else
                        execution.appendOutput("Folder not exists", ERROR);
                }
                break;

            case ConsoleOrder.LIST_ARTISTS:
                if (player.isAlive() && player.hasSounds()) {
                    final List<Artist> listArtists = player.getArtists();
                    for(Artist artist : listArtists)
                        execution.appendOutput(artist.getName(), INFO);
                    execution.appendOutput("------------------------------", INFO);
                    execution.appendOutput("Total: "+listArtists.size(), INFO);
                }
                else
                    execution.appendOutput("The player is not active", WARNING);
                break;

            case ConsoleOrder.LIST_ALBUMS:
                if (player.isAlive() && player.hasSounds()) {
                    List<Album> listAlbums = player.getAlbums();
                    for(Album album : listAlbums) {
                        execution.appendOutput(album.getName(), INFO);
                    }
                    execution.appendOutput("------------------------------", INFO);
                    execution.appendOutput("Total: "+listAlbums.size(), INFO);
                }
                else
                    execution.appendOutput("The player is not active", WARNING);
                break;

            case ConsoleOrder.CHANGE_MODE:
                if (cmd.hasOptions()) {
                    final String firstOpt = cmd.getOptionAt(0);
                    final ConsoleRunner consoleRunner = Global.getInstance().getVar(RUNNER);
                    if (firstOpt.equalsIgnoreCase(RunnerMode.LOCAL.name())) {
                        if (consoleRunner instanceof DaemonRunner) {
                            final LocalRunner localRunner = new LocalRunner(player);
                            TaskRunner.execute(localRunner, localRunner.getClass().getSimpleName());
                            Global.getInstance().setVar(RUNNER, localRunner);
                            ((DaemonRunner)consoleRunner).shutdown();
                            execution.appendOutput("MuPlayer changed from DAEMON to LOCAL mode!", INFO);
                        }
                        else
                            execution.appendOutput("MuPlayer already working with LOCAL mode", WARNING);
                    }
                    else if (firstOpt.equalsIgnoreCase(RunnerMode.DAEMON.name())) {
                        if (consoleRunner instanceof LocalRunner) {
                            final DaemonRunner daemonRunner = new DaemonRunner(player);
                            TaskRunner.execute(daemonRunner, daemonRunner.getClass().getSimpleName());
                            Global.getInstance().setVar(RUNNER, daemonRunner);
                            ((LocalRunner)consoleRunner).shutdown();
                            execution.appendOutput("MuPlayer changed from LOCAL to DAEMON mode!", INFO);
                        }
                        else
                            execution.appendOutput("MuPlayer already working with DAEMON mode", WARNING);
                    }
                    else
                        execution.appendOutput("Option selected unknown, the options must be LOCAL or DAEMON", WARNING);
                }
                else
                    execution.appendOutput("No options selected, the options must be LOCAL or DAEMON", WARNING);
                break;

            default:
                execution.appendOutput("Comando desconocido, inserte el comando \"h\" o \"help\"\n" +
                        "para desplegar el menú de ayuda.", WARNING);
                break;
        }

        return execution;
    }
}
