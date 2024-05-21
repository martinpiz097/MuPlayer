package org.muplayer.console;

import org.muplayer.audio.player.MusicPlayer;
import org.muplayer.audio.track.Track;
import org.muplayer.audio.player.Player;
import org.muplayer.console.runner.ConsoleRunner;
import org.muplayer.console.runner.LocalRunner;
import org.muplayer.console.runner.RunnerMode;
import org.muplayer.data.CacheManager;
import org.muplayer.data.json.command.model.ConsoleCodesData;
import org.muplayer.model.Album;
import org.muplayer.model.Artist;
import org.muplayer.model.SeekOption;
import org.muplayer.console.runner.DaemonRunner;
import org.muplayer.data.json.command.ConsoleCodesInfo;
import org.muplayer.system.*;
import org.muplayer.thread.TaskRunner;
import org.muplayer.util.CollectionUtil;
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
import static org.muplayer.data.CacheVar.RUNNER;

public class ConsoleInterpreter implements CommandInterpreter {
    private Player player;
    private final File playerFolder;
    private boolean on;

    private final CacheManager globalCacheManager;
    private final ConsoleCodesInfo consoleCodesInfo;

    private static final String CMD_DIVISOR = " && ";

    public ConsoleInterpreter(Player player) {
        this.player = player;
        this.playerFolder = player.getRootFolder();
        this.globalCacheManager = CacheManager.getGlobalCache();
        this.consoleCodesInfo = ConsoleCodesInfo.getInstance();
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

        execution.appendOutput("------------------------------", info);
        if (rootFolder == null)
            execution.appendOutput("Music in folder", info);
        else
            execution.appendOutput("Music in folder "+rootFolder.getName(), info);
        execution.appendOutput("------------------------------", info);

        if (rootFolder != null) {
            Track track;
            File fileTrack;
            for (int i = 0; i < player.getSongsCount(); i++) {
                track = listTracks.get(i);
                fileTrack = track.getDataSource();
                if (current != null && fileTrack.getPath().equals(((File)current.getDataSource()).getPath()))
                    execution.appendOutput("Track "+(i+1)+": "
                            +fileTrack.getName(), warn);
                else
                    execution.appendOutput("Track "+(i+1)+": "
                            +fileTrack.getName(), info);
            }
            execution.appendOutput("------------------------------", info);
        }
    }

    private void printDetailedTracks(ConsoleExecution execution) {
        final File rootFolder = player.getRootFolder();
        final List<Track> listTracks = player.getTracks();
        //final List<String> listFolderPaths = player.getListFolders().stream()
        //        .map(File::getPath).collect(Collectors.toList());
        final Track current = player.getCurrent();

        execution.appendOutput("------------------------------", info);
        if (rootFolder == null)
            execution.appendOutput("Music in folder", info);
        else
            execution.appendOutput("Music in folder "+rootFolder.getName(), info);
        execution.appendOutput("------------------------------", info);

        if (rootFolder != null) {
            File trackFile;
            File trackFolder, prevTrackFolder = null;
            for (int i = 0; i < player.getSongsCount(); i++) {
                trackFile = listTracks.get(i).getDataSource();
                trackFolder = trackFile.getParentFile();
                if (prevTrackFolder == null || !trackFolder.getPath().equals(prevTrackFolder.getPath()))
                    execution.appendOutput((prevTrackFolder != null
                            ? "----------------------------------------------------------------------" +
                            "\n\n----------------------------------------------------------------------" +
                            "\nFolder: "
                            : "----------------------------------------------------------------------\n"
                            + "Folder: ")+trackFolder.getName(), info);
                if (current != null && trackFile.getPath().equals(current.getDataSource().getPath()))
                    execution.appendOutput("\tTrack "+(i+1)+": "
                            +trackFile.getName(), warn);
                else
                    execution.appendOutput("\tTrack "+(i+1)+": "
                            +trackFile.getName(), info);

                prevTrackFolder = trackFile.getParentFile();
            }
            execution.appendOutput("------------------------------", info);
        }
    }

    private synchronized void printFolderTracks(ConsoleExecution execution) {
        final List<Track> listTracks = player.getTracks();
        final Track current = player.getCurrent();
        final int songsCount = player.getSongsCount();

        File parentFolder = current == null ? null : current.getDataSource().getParentFile();

        execution.appendOutput("------------------------------", info);
        if (parentFolder == null)
            execution.appendOutput("Music in current folder", info);
        else
            execution.appendOutput("Music in folder "+parentFolder.getName(), info);
        execution.appendOutput("------------------------------", info);

        if (parentFolder != null) {
            File fileTrack;
            File currentFile = current.getDataSource();

            for (int i = 0; i < songsCount; i++) {
                fileTrack = listTracks.get(i).getDataSource();
                if (fileTrack.getParentFile().equals(parentFolder)) {
                    if (fileTrack.getPath().equals(currentFile.getPath()))
                        execution.appendOutput("Track "+(i+1)+": "
                                +fileTrack.getName(), warn);
                    else
                        execution.appendOutput("Track "+(i+1)+": "
                                +fileTrack.getName(), info);
                }
            }
            execution.appendOutput("------------------------------", info);
        }
    }

    private synchronized void printFolderTracks(ConsoleExecution execution, int index) {
        final File folder = player.getListFolders().get(index-1);
        final File currentFile = player.getCurrent().getDataSource();

        execution.appendOutput("------------------------------", info);

        if (folder != null) {
            execution.appendOutput("Music in folder "+folder.getName(), info);
            execution.appendOutput("------------------------------", info);

            final AtomicInteger counter = new AtomicInteger(1);
            player.getTracks().stream().filter(track->track.getDataSource().getParent()
                    .equals(folder.getPath())).forEach(track->{
                        File fileTrack = track.getDataSource();
                        if (fileTrack.getParentFile().equals(folder)) {
                            if (fileTrack.getPath().equals(currentFile.getPath()))
                                execution.appendOutput("Track "+(counter.getAndIncrement())+": "
                                        +fileTrack.getName(), warn);
                            else
                                execution.appendOutput("Track "+(counter.getAndIncrement())+": "
                                        +fileTrack.getName(), info);
                        }
            });
            execution.appendOutput("------------------------------", info);
        }
    }

    private synchronized void printFolders(ConsoleExecution execution) {
        final File rootFolder = player.getRootFolder();
        final List<String> listFolderPaths = player.getListFolders().stream().map(
                File::getPath).collect(Collectors.toList());
        final Track current = player.getCurrent();

        execution.appendOutput("------------------------------", info);
        if (rootFolder == null)
            execution.appendOutput("Folders", info);
        else
            execution.appendOutput("Folders in "+rootFolder.getName(), info);
        execution.appendOutput("------------------------------", info);

        if (current == null)
            return;

        File currentTrackFile = current.getDataSource();
        File folder;

        for (int i = 0; i < player.getFoldersCount(); i++) {
            folder = new File(listFolderPaths.get(i));
            if (folder.getPath().equals(currentTrackFile.getParentFile().getPath())) {
                execution.appendOutput("Folder "+(i+1)+": "
                        +folder.getName(), warn);
            }
            else {
                execution.appendOutput("Folder "+(i+1)+": "
                        +folder.getName(), info);
            }

        }
        execution.appendOutput("------------------------------", info);
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
        final String keyValueSeparator = ":\n\t";
        final String helpElementSeparator = "\n\n";
        final String orderSelementsSeparator = ",";

        var consoleCodesDataMap = consoleCodesInfo.getJsonSource().getData();
        String helpInfoData = consoleCodesDataMap.parallelStream()
                .sorted(Comparator.comparing(ConsoleCodesData::getCode))
                .sequential()
                .map(consoleCodesData -> consoleCodesData.getJoinedOrders(orderSelementsSeparator)
                        + keyValueSeparator
                        + consoleCodesData.getHelpInfo())
                .collect(Collectors.joining(helpElementSeparator));

        execution.appendOutput("---------", info);
        execution.appendOutput("Help Info", info);
        execution.appendOutput("---------", info);
        execution.appendOutput(helpInfoData, info);
    }

    public void showSongInfo(Track track, ConsoleExecution execution) {
        if (track == null)
            execution.appendOutput("Current track unavailable", error);
        else
            execution.appendOutput(TrackUtil.getSongInfo(track), warn);
    }

    public ConsoleExecution executeCommand(String cmdString) throws Exception {
        if (cmdString.contains(CMD_DIVISOR)) {
            final String[] cmdSplit = cmdString.split(CMD_DIVISOR);
            final List<String> listExec = CollectionUtil.newFastList();
            ConsoleExecution exec;

            for (int i = 0; i < cmdSplit.length; i++) {
                exec = executeCommand(new Command(cmdSplit[i].trim()));
                if (exec != null)
                    listExec.add(exec.getOutputMsg());
            }

            ConsoleExecution consoleExecution = new ConsoleExecution(cmdString);
            consoleExecution.appendOutput(listExec.get(listExec.size()-1), null);
            return consoleExecution;
        }
        else {
            return executeCommand(new Command(cmdString));
        }
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
        final ConsoleOrderCode consoleOrderCode = consoleCodesInfo.getConsoleOrderCodeByCmdOrder(cmdOrder);
        Track current;

        final ConsoleExecution execution = new ConsoleExecution(cmd);
        // imprimir output de este objeto no mas

        if (consoleOrderCode != null) {
            switch (consoleOrderCode) {
                case st:
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
                            execution.appendOutput("Folder not exists", error);
                    } else if (!player.isAlive())
                        player.start();
                    if (player.getCurrent() != null) {
                        showSongInfo(player.getCurrent(), execution);
                    }
                    break;
                case ist:
                    execution.appendOutput(isPlayerOn() ? "Is playing" : "Is not playing", warn);
                    break;

                case pl:
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

                case ps:
                    if (isPlayerOn())
                        player.pause();
                    break;

                case r:
                    if (isPlayerOn())
                        player.resumeTrack();
                    break;

                case n:
                    if (isPlayerOn()) {
                        if (cmd.hasOptions()) {
                            Number jumps = cmd.getOptionAsNumber(0);
                            if (jumps == null)
                                execution.appendOutput("Jump value incorrect", error);
                            else
                                player.jumpTrack(jumps.intValue(), SeekOption.NEXT);
                        }
                        else
                            player.playNext();
                    }
                    showSongInfo(player.getCurrent(), execution);
                    break;

                case p:
                    if (isPlayerOn()) {
                        if (cmd.hasOptions()) {
                            Number jumps = cmd.getOptionAsNumber(0);
                            if (jumps == null)
                                execution.appendOutput("Jump value incorrect", error);
                            else
                                player.jumpTrack(jumps.intValue(), SeekOption.PREV);
                        } else
                            player.playPrevious();
                        showSongInfo(player.getCurrent(), execution);
                    }
                    break;

                case m:
                    if (isPlayerOn())
                        player.mute();
                    break;

                case um:
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

                case l:
                    if (player != null)
                        printTracks(execution);
                    break;

                case lc:
                    if (isPlayerOn())
                        printFolderTracks(execution);
                    break;

                case lf:
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

                case ld:
                    if (isPlayerOn())
                        printDetailedTracks(execution);
                    break;

                case gv:
                    if (player != null)
                        execution.appendOutput("Player Volume(0-100): " + player.getVolume(), warn);
                    break;

                case v:
                    if (player != null)
                        if (cmd.hasOptions()) {
                            Number volume = cmd.getOptionAsNumber(0);
                            if (volume == null)
                                execution.appendOutput("Volume value incorrect", error);
                            else {
                                player.setVolume(volume.floatValue());
                                execution.appendOutput("Volume value changed", warn);
                            }
                        }
                    break;
                case gsv:
                    if (player != null)
                        execution.appendOutput("Player Volume(0-100): " + player.getSystemVolume(), warn);
                    break;

                case sv:
                    if (player != null)
                        if (cmd.hasOptions()) {
                            Number volume = cmd.getOptionAsNumber(0);
                            if (volume == null)
                                execution.appendOutput("Volume value incorrect", error);
                            else {
                                player.setSystemVolume(volume.floatValue());
                                execution.appendOutput("Volume value changed", warn);
                            }
                        }
                    break;

                case sh:
                    if (isPlayerOn()) {
                        player.shutdown();
                        player = null;
                    }
                    on = false;
                    break;

                case k:
                    if (isPlayerOn())
                        if (cmd.hasOptions()) {
                            Number seekSec = cmd.getOptionAsNumber(0);
                            if (seekSec == null)
                                execution.appendOutput("Seek value incorrect", error);
                            else {
                                player.seek(seekSec.doubleValue());
                            }
                        }
                    break;
                case skf:
                    if (isPlayerOn()) {
                        if (cmd.hasOptions()) {
                            String optionParam = cmd.getOptionAt(0);
                            SeekOption option = optionParam.equals("next") ? SeekOption.NEXT
                                    : (optionParam.equals("prev") ? SeekOption.PREV : null);
                            Number jumps = cmd.getOptionAsNumber(1);

                            if (cmd.getOptionsCount() > 1 && jumps == null)
                                execution.appendOutput("Seek value incorrect", error);
                            else {
                                if (jumps == null && option == null)
                                    execution.appendOutput("Option value incorrect", error);
                                else if (jumps == null)
                                    player.seekFolder(option);
                                else if (jumps.intValue() < 0)
                                    execution.appendOutput("Jumps value incorrect", error);
                                else
                                    player.seekFolder(option, jumps.intValue());
                            }

                        } else
                            player.seekFolder(SeekOption.NEXT);
                        showSongInfo(player.getCurrent(), execution);
                    }
                    break;

                case u:
                    if (player != null)
                        player.reload();
                    break;
                case g:
                    if (player != null)
                        if (cmd.hasOptions()) {
                            Number gotoSec = cmd.getOptionAsNumber(0);
                            if (gotoSec == null)
                                execution.appendOutput("Go to value incorrect", error);
                            else
                                player.gotoSecond(gotoSec.doubleValue());
                        }
                    break;
                case c:
                    final int count = player.getSongsCount();
                    if (player != null)
                        execution.appendOutput(player.getSongsCount(), info);
                    break;
                case d:
                    if (player != null)
                        execution.appendOutput(player.getCurrent().getFormattedDuration(), info);
                    break;
                case cover:
                    current = player.getCurrent();
                    if (current == null)
                        execution.appendOutput("Current track unavailable", error);
                    else if (!current.hasCover())
                        execution.appendOutput("Current song don't have cover", error);
                    else if (cmd.hasOptions()) {
                        File folderPath = new File(cmd.getOptionAt(0));
                        if (!folderPath.exists())
                            folderPath = player.getRootFolder();
                        File fileCover = new File(folderPath, "cover-" + current.getTitle() + ".png");
                        fileCover.createNewFile();
                        Files.write(fileCover.toPath(), current.getCoverData(), WRITE);
                        execution.appendOutput("Created cover with name " + fileCover.getName(), warn);
                    } else
                        execution.appendOutput("Cover path not defined", error);
                    break;

                case info:
                    if (player.hasSounds() && player.getCurrent() != null)
                        showSongInfo(player.getCurrent(), execution);
                    else
                        execution.appendOutput("No song available", warn);
                    break;

                case prog:
                    current = player.getCurrent();
                    if (current == null) {
                        execution.appendOutput("Current track unavailable", error);
                    }
                    else {
                        final String formattedProgress = current.getFormattedProgress();;
                        final String formattedDuration = current.getFormattedDuration();
                        execution.appendOutput(formattedProgress+"/"+formattedDuration, warn);
                    }
                    break;

                case cls:
                    clearConsole();
                    break;

                case format:
                    current = player.getCurrent();
                    if (current == null)
                        execution.appendOutput("Current track unavailable", error);
                    else {
                        final String className = current.getClass().getSimpleName();
                        execution.appendOutput(className.substring(0, className.length() - 5).toLowerCase(), warn);
                    }
                    break;

                case title:
                    current = player.getCurrent();
                    if (current == null)
                        execution.appendOutput("Current track unavailable", error);
                    else
                        execution.appendOutput(current.getTitle(), warn);
                    break;

                case name:
                    current = player.getCurrent();
                    if (current == null)
                        execution.appendOutput("Current track unavailable", error);
                    else
                        execution.appendOutput(current.getDataSource().getName(), warn);
                    break;

                case h:
                    printHelp(execution);
                    break;

                case sys:
                    if (cmd.hasOptions())
                        execSysCommand(cmd.getOptionsAsString());
                    break;

                case sn:
                    if (isPlayerOn())
                        execution.appendOutput(TrackUtil.getSongInfo(player.getNext()), warn);
                    break;

                case sp:
                    if (isPlayerOn())
                        execution.appendOutput(TrackUtil.getSongInfo(player.getPrevious()), warn);
                    break;

                case pf:
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

                case load:
                    if (cmd.hasOptions()) {
                        File folder = new File(cmd.getOptionAt(0));
                        if (folder.exists()) {
                            if (folder.isDirectory()) {
                                if (folder.list() == null)
                                    execution.appendOutput("Folder is empty", error);
                                else {
                                    final Player newMusicPlayer = new MusicPlayer(folder);
                                    newMusicPlayer.start();
                                    if (isPlayerOn())
                                        player.shutdown();
                                    player = newMusicPlayer;
                                }
                            }
                            else
                                execution.appendOutput("Path not be a directory", error);
                        }
                        else
                            execution.appendOutput("Folder not exists", error);
                    }
                    break;

                case arts:
                    if (player.isAlive() && player.hasSounds()) {
                        final List<Artist> listArtists = player.getArtists();
                        for(Artist artist : listArtists)
                            execution.appendOutput(artist.getName(), info);
                        execution.appendOutput("------------------------------", info);
                        execution.appendOutput("Total: "+listArtists.size(), info);
                    }
                    else
                        execution.appendOutput("The player is not active", warn);
                    break;

                case albs:
                    if (player.isAlive() && player.hasSounds()) {
                        List<Album> listAlbums = player.getAlbums();
                        for(Album album : listAlbums) {
                            execution.appendOutput(album.getName(), info);
                        }
                        execution.appendOutput("------------------------------", info);
                        execution.appendOutput("Total: "+listAlbums.size(), info);
                    }
                    else
                        execution.appendOutput("The player is not active", warn);
                    break;

                case chm:
                    if (cmd.hasOptions()) {
                        final String firstOpt = cmd.getOptionAt(0);
                        final ConsoleRunner consoleRunner = globalCacheManager.loadValue(RUNNER);
                        if (firstOpt.equalsIgnoreCase(RunnerMode.LOCAL.name())) {
                            if (consoleRunner instanceof DaemonRunner) {
                                final LocalRunner localRunner = new LocalRunner(player);
                                TaskRunner.execute(localRunner, localRunner.getClass().getSimpleName());
                                globalCacheManager.saveValue(RUNNER, localRunner);
                                ((DaemonRunner)consoleRunner).shutdown();
                                execution.appendOutput("MuPlayer changed from DAEMON to LOCAL mode!", info);
                            }
                            else
                                execution.appendOutput("MuPlayer already working with LOCAL mode", warn);
                        }
                        else if (firstOpt.equalsIgnoreCase(RunnerMode.DAEMON.name())) {
                            if (consoleRunner instanceof LocalRunner) {
                                final DaemonRunner daemonRunner = new DaemonRunner(player);
                                TaskRunner.execute(daemonRunner, daemonRunner.getClass().getSimpleName());
                                globalCacheManager.saveValue(RUNNER, daemonRunner);
                                ((LocalRunner)consoleRunner).shutdown();
                                execution.appendOutput("MuPlayer changed from LOCAL to DAEMON mode!", info);
                            }
                            else
                                execution.appendOutput("MuPlayer already working with DAEMON mode", warn);
                        }
                        else
                            execution.appendOutput("Option selected unknown, the options must be LOCAL or DAEMON", warn);
                    }
                    else
                        execution.appendOutput("No options selected, the options must be LOCAL or DAEMON", warn);
                    break;
            }

        }
        else {
            execution.appendOutput("Comando desconocido, inserte el comando \"h\" o \"help\"\n" +
                    "para desplegar el menú de ayuda.", warn);
        }

        return execution;
    }
}
