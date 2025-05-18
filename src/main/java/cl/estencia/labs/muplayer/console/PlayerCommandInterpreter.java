package cl.estencia.labs.muplayer.console;

import cl.estencia.labs.muplayer.audio.player.MuPlayer;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.config.model.ConsoleCodesData;
import cl.estencia.labs.muplayer.config.reader.ConsoleCodesReader;
import cl.estencia.labs.muplayer.console.runner.ConsoleRunner;
import cl.estencia.labs.muplayer.console.runner.DaemonRunner;
import cl.estencia.labs.muplayer.console.runner.LocalRunner;
import cl.estencia.labs.muplayer.console.runner.RunnerMode;
import cl.estencia.labs.muplayer.core.cache.CacheManager;
import cl.estencia.labs.muplayer.audio.model.Album;
import cl.estencia.labs.muplayer.audio.model.Artist;
import cl.estencia.labs.muplayer.core.common.enums.SeekOption;
import cl.estencia.labs.muplayer.service.LogService;
import cl.estencia.labs.muplayer.service.impl.LogServiceImpl;
import cl.estencia.labs.muplayer.system.SysInfo;
import cl.estencia.labs.muplayer.thread.TaskRunner;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import cl.estencia.labs.muplayer.audio.util.TrackUtil;
import lombok.Getter;
import lombok.Setter;
import org.orangelogger.sys.Logger;
import org.orangelogger.sys.SystemUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static cl.estencia.labs.muplayer.console.OutputType.*;
import static cl.estencia.labs.muplayer.core.cache.CacheVar.RUNNER;
import static java.nio.file.StandardOpenOption.WRITE;

public class PlayerCommandInterpreter implements CommandInterpreter {
    private Player player;
    private final File playerFolder;

    @Getter
    @Setter
    private boolean on;

    private final CacheManager globalCacheManager;
    private final ConsoleCodesReader consoleCodesReader;
    private final LogService logService;

    private final TrackUtil trackUtil;

    private static final String CMD_DIVISOR = " && ";

    public PlayerCommandInterpreter(Player player) {
        this.player = player;
        this.playerFolder = player.getRootFolder();
        this.globalCacheManager = CacheManager.getGlobalCache();
        this.consoleCodesReader = ConsoleCodesReader.getInstance();
        this.logService = new LogServiceImpl();
        trackUtil = new TrackUtil();
    }

    private boolean isPlayerOn() {
        return player != null && player.isAlive();
    }

    private void execSysCommand(String cmd) {
        Process process;
        try {
            process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            if (process.exitValue() == 0) {
                printStreamOut(process.getInputStream());
            } else {
                printStreamOut(process.getErrorStream());
            }
        } catch (IOException | InterruptedException e) {
            Logger.getLogger(this, e.getMessage()).error();
        }
    }

    private void printTracks(ConsoleExecution execution) {
        final File rootFolder = player.getRootFolder();
        final List<Track> listTracks = player.getTracks();
        final Track current = player.getCurrentTrack().get();

        execution.appendOutput("------------------------------", info);
        if (rootFolder == null) {
            execution.appendOutput("Music in folder", info);
        } else {
            execution.appendOutput("Music in folder " + rootFolder.getName(), info);
        }
        execution.appendOutput("------------------------------", info);

        if (rootFolder != null) {
            Track track;
            File fileTrack;
            for (int i = 0; i < player.getSongsCount(); i++) {
                track = listTracks.get(i);
                fileTrack = track.getDataSource();
                if (current != null && fileTrack.getPath().equals((current.getDataSource()).getPath())) {
                    execution.appendOutput("Track " + (i + 1) + ": "
                            + fileTrack.getName(), warn);
                } else {
                    execution.appendOutput("Track " + (i + 1) + ": "
                            + fileTrack.getName(), info);
                }
            }
            execution.appendOutput("------------------------------", info);
        }
    }

    private void printDetailedTracks(ConsoleExecution execution) {
        final File rootFolder = player.getRootFolder();
        final List<Track> listTracks = player.getTracks();
        final Track current = player.getCurrentTrack().get();

        execution.appendOutput("------------------------------", info);
        if (rootFolder == null) {
            execution.appendOutput("Music in folder", info);
        } else {
            execution.appendOutput("Music in folder " + rootFolder.getName(), info);
        }
        execution.appendOutput("------------------------------", info);

        if (rootFolder != null) {
            File trackFile;
            File trackFolder, prevTrackFolder = null;
            for (int i = 0; i < player.getSongsCount(); i++) {
                trackFile = listTracks.get(i).getDataSource();
                trackFolder = trackFile.getParentFile();
                if (prevTrackFolder == null || !trackFolder.getPath().equals(prevTrackFolder.getPath())) {
                    execution.appendOutput((prevTrackFolder != null
                            ? "----------------------------------------------------------------------" +
                            "\n\n----------------------------------------------------------------------" +
                            "\nFolder: "
                            : "----------------------------------------------------------------------\n"
                            + "Folder: ") + trackFolder.getName(), info);
                }
                if (current != null && trackFile.getPath().equals(current.getDataSource().getPath())) {
                    execution.appendOutput("\tTrack " + (i + 1) + ": "
                            + trackFile.getName(), warn);
                } else {
                    execution.appendOutput("\tTrack " + (i + 1) + ": "
                            + trackFile.getName(), info);
                }

                prevTrackFolder = trackFile.getParentFile();
            }
            execution.appendOutput("------------------------------", info);
        }
    }

    private synchronized void printFolderTracks(ConsoleExecution execution) {
        final List<Track> listTracks = player.getTracks();
        final Track current = player.getCurrentTrack().get();
        final int songsCount = player.getSongsCount();

        File parentFolder = current == null ? null : current.getDataSource().getParentFile();

        execution.appendOutput("------------------------------", info);
        if (parentFolder == null) {
            execution.appendOutput("Music in current folder", info);
        } else {
            execution.appendOutput("Music in folder " + parentFolder.getName(), info);
        }
        execution.appendOutput("------------------------------", info);

        if (parentFolder != null) {
            File fileTrack;
            File currentFile = current.getDataSource();

            for (int i = 0; i < songsCount; i++) {
                fileTrack = listTracks.get(i).getDataSource();
                if (fileTrack.getParentFile().equals(parentFolder)) {
                    if (fileTrack.getPath().equals(currentFile.getPath())) {
                        execution.appendOutput("Track " + (i + 1) + ": "
                                + fileTrack.getName(), warn);
                    } else {
                        execution.appendOutput("Track " + (i + 1) + ": "
                                + fileTrack.getName(), info);
                    }
                }
            }
            execution.appendOutput("------------------------------", info);
        }
    }

    private synchronized void printFolderTracks(ConsoleExecution execution, int index) {
        final AtomicReference<Track> current = player.getCurrentTrack();
        final File folder = player.getListFolders().get(index - 1);
        final File currentFile = current.get() != null
                ? current.get().getDataSource()
                : null;

        execution.appendOutput("------------------------------", info);

        if (folder != null) {
            execution.appendOutput("Music in folder " + folder.getName(), info);
            execution.appendOutput("------------------------------", info);

            final AtomicInteger counter = new AtomicInteger(1);
            player.getTracks().stream().filter(track -> track.getDataSource().getParent()
                    .equals(folder.getPath())).forEach(track -> {
                File fileTrack = track.getDataSource();
                if (fileTrack.getParentFile().equals(folder)) {
                    if (fileTrack.getPath().equals(currentFile.getPath())) {
                        execution.appendOutput("Track " + (counter.getAndIncrement()) + ": "
                                + fileTrack.getName(), warn);
                    } else {
                        execution.appendOutput("Track " + (counter.getAndIncrement()) + ": "
                                + fileTrack.getName(), info);
                    }
                }
            });
            execution.appendOutput("------------------------------", info);
        }
    }

    private synchronized void printFolders(ConsoleExecution execution) {
        final File rootFolder = player.getRootFolder();
        final List<String> listFolderPaths = player.getListFolders()
                .stream().map(File::getPath).toList();
        final Track current = player.getCurrentTrack().get();

        execution.appendOutput("------------------------------", info);
        if (rootFolder == null) {
            execution.appendOutput("Folders", info);
        } else {
            execution.appendOutput("Folders in " + rootFolder.getName(), info);
        }
        execution.appendOutput("------------------------------", info);

        if (current == null) {
            return;
        }

        File currentTrackFile = current.getDataSource();
        File folder;

        for (int i = 0; i < player.getFoldersCount(); i++) {
            folder = new File(listFolderPaths.get(i));
            if (folder.getPath().equals(currentTrackFile.getParentFile().getPath())) {
                execution.appendOutput("Folder " + (i + 1) + ": "
                        + folder.getName(), warn);
            } else {
                execution.appendOutput("Folder " + (i + 1) + ": "
                        + folder.getName(), info);
            }

        }
        execution.appendOutput("------------------------------", info);
    }

    protected void printStreamOut(InputStream cmdStream) throws IOException {
        int read;
        FileOutputStream stdout = SystemUtil.getStdout();
        while ((read = cmdStream.read()) != -1) {
            stdout.write(read);
        }
    }

    protected void clearConsole() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(SysInfo.IS_UNIX ? "clear" : "cls");
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final int exitValue = process.exitValue();
        printStreamOut(exitValue == 0 ? process.getInputStream() : process.getErrorStream());
    }

    protected void printHelp(ConsoleExecution execution) {
        final String keyValueSeparator = ":\n\t";
        final String helpElementSeparator = "\n\n";
        final String orderSelementsSeparator = ",";

        var consoleCodesDataMap = consoleCodesReader.getJsonSource().getData();
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
        if (track == null) {
            execution.appendOutput("Current track unavailable", error);
        } else {
            execution.appendOutput(trackUtil.getSongInfo(track), warn);
        }
    }

    public ConsoleExecution executeCommand(String cmdString) throws Exception {
        if (cmdString.contains(CMD_DIVISOR)) {
            final String[] cmdSplit = cmdString.split(CMD_DIVISOR);
            final List<String> listExec = CollectionUtil.newLinkedList();
            ConsoleExecution exec;

            for (int i = 0; i < cmdSplit.length; i++) {
                exec = executeCommand(new Command(cmdSplit[i].trim()));
                if (exec != null) {
                    listExec.add(exec.getOutputMsg());
                }
            }

            ConsoleExecution consoleExecution = new ConsoleExecution(cmdString);
            consoleExecution.appendOutput(listExec.get(listExec.size() - 1), null);
            return consoleExecution;
        } else {
            return executeCommand(new Command(cmdString));
        }
    }

    @Override
    public ConsoleExecution executeCommand(Command cmd) throws Exception {
        final String cmdOrder = cmd.getOrder();
        final ConsoleOrderCode consoleOrderCode = consoleCodesReader.getConsoleOrderCodeByCmdOrder(cmdOrder);
        final AtomicReference<Track> currentRef = player != null
                ? player.getCurrentTrack()
                : new AtomicReference<>(null);
        final Track currentTrack = currentRef.get();

        final ConsoleExecution execution = new ConsoleExecution(cmd);
        // imprimir output de este objeto no mas

        if (consoleOrderCode != null) {
            switch (consoleOrderCode) {
                case st -> {
                    if (player == null) {
                        player = new MuPlayer(playerFolder);
                    }
                    if (player.isAlive() && cmd.hasOptions()) {
                        File musicFolder = new File(cmd.getOptionAt(0));
                        if (musicFolder.exists()) {
                            Player newMusicPlayer = new MuPlayer(musicFolder);
                            player.shutdown();
                            newMusicPlayer.start();
                            player = newMusicPlayer;
                        } else {
                            execution.appendOutput("Folder not exists", error);
                        }
                    } else if (!player.isAlive()) {
                        player.start();
                    }
                    if (currentTrack != null) {
                        showSongInfo(currentTrack, execution);
                    }
                }
                case ist -> execution.appendOutput(isPlayerOn() ? "Is playing" : "Is not playing", warn);
                case pl -> {
                    if (isPlayerOn()) {
                        if (cmd.hasOptions()) {
                            Number playIndex = cmd.getOptionAsNumber(0);
                            if (playIndex != null && playIndex.intValue() > 0 && playIndex.intValue() <= player.getSongsCount()) {
                                player.play(playIndex.intValue() - 1);
                            }
                            showSongInfo(player.getCurrentTrack().get(), execution);
                        } else {
                            player.play();
                        }
                    }
                }
                case ps -> {
                    if (isPlayerOn()) {
                        player.pause();
                    }
                }
                case r -> {
                    if (isPlayerOn()) {
                        player.resumeTrack();
                    }
                }
                case s -> {
                    if (isPlayerOn()) {
                        player.stopTrack();
                    }
                }
                case n -> {
                    if (isPlayerOn()) {
                        if (cmd.hasOptions()) {
                            Number jumps = cmd.getOptionAsNumber(0);
                            if (jumps == null) {
                                execution.appendOutput("Jump value incorrect", error);
                            } else {
                                player.jumpTrack(jumps.intValue(), SeekOption.NEXT);
                            }
                        } else {
                            player.playNext();
                        }
                        showSongInfo(player.getCurrentTrack().get(), execution);
                    }
                }
                case p -> {
                    if (isPlayerOn()) {
                        if (cmd.hasOptions()) {
                            Number jumps = cmd.getOptionAsNumber(0);
                            if (jumps == null) {
                                execution.appendOutput("Jump value incorrect", error);
                            } else {
                                player.jumpTrack(jumps.intValue(), SeekOption.PREV);
                            }
                        } else {
                            player.playPrevious();
                        }
                        showSongInfo(player.getCurrentTrack().get(), execution);
                    }
                }
                case m -> {
                    if (isPlayerOn()) {
                        player.mute();
                    }
                }
                case um -> {
                    if (isPlayerOn()) {
                        player.unMute();
                    }
                }
                case l -> {
                    if (player != null) {
                        printTracks(execution);
                    }
                }
                case lc -> {
                    if (isPlayerOn()) {
                        printFolderTracks(execution);
                    }
                }
                case lf -> {
                    if (isPlayerOn()) {
                        if (cmd.hasOptions()) {
                            try {
                                Number index = cmd.getOptionAsNumber(0);
                                printFolderTracks(execution, index.intValue());
                            } catch (NumberFormatException e) {
                            }
                        } else {
                            printFolders(execution);
                        }
                    }
                }
                case ld -> {
                    if (isPlayerOn()) {
                        printDetailedTracks(execution);
                    }
                }
                case gv -> {
                    if (player != null) {
                        execution.appendOutput("Player Volume(0-100): " + player.getVolume(), warn);
                    }
                }
                case v -> {
                    if (player != null && cmd.hasOptions()) {
                        Number volume = cmd.getOptionAsNumber(0);
                        if (volume == null) {
                            execution.appendOutput("Volume value incorrect", error);
                        } else {
                            player.setVolume(volume.floatValue());
                            execution.appendOutput("Volume value changed", warn);
                        }
                    }
                }
                case gsv -> {
                    if (player != null) {
                        execution.appendOutput("Player Volume(0-100): " + player.getSystemVolume(), warn);
                    }
                }
                case sv -> {
                    if (player != null && cmd.hasOptions()) {
                        Number volume = cmd.getOptionAsNumber(0);
                        if (volume == null) {
                            execution.appendOutput("Volume value incorrect", error);
                        } else {
                            player.setSystemVolume(volume.floatValue());
                            execution.appendOutput("Volume value changed", warn);
                        }
                    }
                }
                case sh -> {
                    if (isPlayerOn()) {
                        player.shutdown();
                        player = null;
                    }
                    on = false;
                }
                case sk -> {
                    if (isPlayerOn() && cmd.hasOptions()) {
                        Number seekSec = cmd.getOptionAsNumber(0);
                        if (seekSec == null) {
                            execution.appendOutput("Seek value incorrect", error);
                        } else {
                            player.seek(seekSec.doubleValue());
                        }
                    }
                }
                case skf -> {
                    if (isPlayerOn()) {
                        if (cmd.hasOptions()) {
                            String optionParam = cmd.getOptionAt(0);
                            SeekOption option = optionParam.equals("next") ? SeekOption.NEXT
                                    : (optionParam.equals("prev") ? SeekOption.PREV : null);
                            Number jumps = cmd.getOptionAsNumber(1);

                            if (cmd.getOptionsCount() > 1 && jumps == null) {
                                execution.appendOutput("Seek value incorrect", error);
                            } else {
                                if (jumps == null && option == null) {
                                    execution.appendOutput("Option value incorrect", error);
                                } else if (jumps == null) {
                                    player.seekFolder(option);
                                } else if (jumps.intValue() < 0) {
                                    execution.appendOutput("Jumps value incorrect", error);
                                } else {
                                    player.seekFolder(option, jumps.intValue());
                                }
                            }
                        } else {
                            player.seekFolder(SeekOption.NEXT);
                        }
                        showSongInfo(currentTrack, execution);
                    }
                }
                case u -> {
                    if (player != null) {
                        player.reload();
                    }
                }
                case g -> {
                    if (player != null && cmd.hasOptions()) {
                        Number gotoSec = cmd.getOptionAsNumber(0);
                        if (gotoSec == null) {
                            execution.appendOutput("Go to value incorrect", error);
                        } else {
                            player.gotoSecond(gotoSec.doubleValue());
                        }
                    }
                }
                case c -> {
                    if (player != null) {
                        execution.appendOutput(player.getSongsCount(), info);
                    }
                }
                case d -> {
                    if (player != null) {
                        String formattedDuration = currentTrack != null ? currentTrack.getFormattedDuration() : "";
                        execution.appendOutput(formattedDuration, info);
                    }
                }
                case cover -> {
                    if (currentTrack == null) {
                        execution.appendOutput("Current track unavailable", error);
                    } else if (!currentTrack.hasCover()) {
                        execution.appendOutput("Current song don't have cover", error);
                    } else if (cmd.hasOptions()) {
                        File folderPath = new File(cmd.getOptionAt(0));
                        if (!folderPath.exists()) {
                            folderPath = player.getRootFolder();
                        }
                        File fileCover = new File(folderPath, "cover-" + currentTrack.getTitle() + ".png");
                        fileCover.createNewFile();
                        Files.write(fileCover.toPath(), currentTrack.getCoverData(), WRITE);
                        execution.appendOutput("Created cover with name " + fileCover.getName(), warn);
                    } else {
                        execution.appendOutput("Cover path not defined", error);
                    }
                }
                case info -> {
                    if (player != null) {
                        if (player.hasSounds() && player.getCurrentTrack() != null) {
                            showSongInfo(currentTrack, execution);
                        } else {
                            execution.appendOutput("No song available", warn);
                        }
                    } else {
                        execution.appendOutput("No song available, player not initialized", warn);
                    }
                }
                case prog -> {
                    if (currentTrack == null) {
                        execution.appendOutput("Current track unavailable", error);
                    } else {
                        final String formattedProgress = currentTrack.getFormattedProgress();
                        final String formattedDuration = currentTrack.getFormattedDuration();
                        execution.appendOutput(formattedProgress + "/" + formattedDuration, warn);
                    }
                }
                case cls -> clearConsole();
                case format -> {
                    if (currentTrack == null) {
                        execution.appendOutput("Current track unavailable", error);
                    } else {
                        final String className = currentTrack.getClass().getSimpleName();
                        execution.appendOutput(className.substring(0, className.length() - 5).toLowerCase(), warn);
                    }
                }
                case title -> {
                    if (currentTrack == null) {
                        execution.appendOutput("Current track unavailable", error);
                    } else {
                        execution.appendOutput(currentTrack.getTitle(), warn);
                    }
                }
                case name -> {
                    if (currentTrack == null) {
                        execution.appendOutput("Current track unavailable", error);
                    } else {
                        execution.appendOutput(currentTrack.getDataSource().getName(), warn);
                    }
                }
                case h -> printHelp(execution);
                case sys -> {
                    if (cmd.hasOptions()) {
                        execSysCommand(cmd.getOptionsAsString());
                    }
                }
                case sn -> {
                    if (isPlayerOn()) {
                        execution.appendOutput(trackUtil.getSongInfo(player.getNext()), warn);
                    }
                }
                case sp -> {
                    if (isPlayerOn()) {
                        execution.appendOutput(trackUtil.getSongInfo(player.getPrevious()), warn);
                    }
                }
                case pf -> {
                    if (isPlayerOn() && cmd.hasOptions()) {
                        final Number fldIndex = cmd.getOptionAsNumber(0);
                        if (fldIndex != null && fldIndex.intValue() > 0) {
                            player.playFolder(fldIndex.intValue() - 1);
                            showSongInfo(currentTrack, execution);
                        }
                    }
                }
                case load -> {
                    if (cmd.hasOptions()) {
                        File folder = new File(cmd.getOptionAt(0));
                        if (folder.exists()) {
                            if (folder.isDirectory()) {
                                if (folder.list() == null) {
                                    execution.appendOutput("Folder is empty", error);
                                } else {
                                    final Player newMusicPlayer = new MuPlayer(folder);
                                    newMusicPlayer.start();
                                    if (isPlayerOn()) {
                                        player.shutdown();
                                    }
                                    player = newMusicPlayer;
                                }
                            } else {
                                execution.appendOutput("Path not be a directory", error);
                            }
                        } else {
                            execution.appendOutput("Folder not exists", error);
                        }
                    }
                }
                case arts -> {
                    if (player != null && player.isAlive() && player.hasSounds()) {
                        final List<Artist> listArtists = player.getArtists();
                        for (Artist artist : listArtists) {
                            execution.appendOutput(artist.getName(), info);
                        }
                        execution.appendOutput("------------------------------", info);
                        execution.appendOutput("Total: " + listArtists.size(), info);
                    } else {
                        execution.appendOutput("The player is not active", warn);
                    }
                }
                case albs -> {
                    if (player != null && player.isAlive() && player.hasSounds()) {
                        List<Album> listAlbums = player.getAlbums();
                        for (Album album : listAlbums) {
                            execution.appendOutput(album.getName(), info);
                        }
                        execution.appendOutput("------------------------------", info);
                        execution.appendOutput("Total: " + listAlbums.size(), info);
                    } else {
                        execution.appendOutput("The player is not active", warn);
                    }
                }
                case chm -> {
                    if (cmd.hasOptions()) {
                        final String firstOpt = cmd.getOptionAt(0);
                        final ConsoleRunner consoleRunner = globalCacheManager.loadValue(RUNNER);
                        if (firstOpt.equalsIgnoreCase(RunnerMode.LOCAL.name())) {
                            if (consoleRunner instanceof DaemonRunner) {
                                final LocalRunner localRunner = new LocalRunner(player);
                                TaskRunner.execute(localRunner, localRunner.getClass().getSimpleName());
                                globalCacheManager.saveValue(RUNNER, localRunner);
                                ((DaemonRunner) consoleRunner).shutdown();
                                execution.appendOutput("MuPlayer changed from DAEMON to LOCAL mode!", info);
                            } else {
                                execution.appendOutput("MuPlayer already working with LOCAL mode", warn);
                            }
                        } else if (firstOpt.equalsIgnoreCase(RunnerMode.DAEMON.name())) {
                            if (consoleRunner instanceof LocalRunner) {
                                final DaemonRunner daemonRunner = new DaemonRunner(player);
                                TaskRunner.execute(daemonRunner, daemonRunner.getClass().getSimpleName());
                                globalCacheManager.saveValue(RUNNER, daemonRunner);
                                ((LocalRunner) consoleRunner).shutdown();
                                execution.appendOutput("MuPlayer changed from LOCAL to DAEMON mode!", info);
                            } else {
                                execution.appendOutput("MuPlayer already working with DAEMON mode", warn);
                            }
                        } else {
                            execution.appendOutput("Option selected unknown, the options must be LOCAL or DAEMON", warn);
                        }
                    } else {
                        execution.appendOutput("No options selected, the options must be LOCAL or DAEMON", warn);
                    }
                }
                case smf -> {
                    if (cmd.hasNotOptions()) {
                        logService.errorLog("[Set music folder]\nCommand use: \n\tsmf ${music-folder-path}\n");
                    } else {
                        String musicFolderPath = cmd.getOptionAt(0);
                        File musicFolderFile = new File(musicFolderPath);
                        if (!musicFolderFile.exists()) {
                            logService.errorLog("[" + musicFolderFile + "] doesn't exist\n");
                        } else if (!musicFolderFile.isDirectory()) {
                            logService.errorLog("[" + musicFolderFile + "] is not a folder\n");
                        } else {
                            player.addMusic(musicFolderFile);
                        }
                    }
                }
            }

        } else {
            execution.appendOutput("Comando desconocido, inserte el comando \"h\" o \"help\"\n" +
                    "para desplegar el menú de ayuda.", warn);
        }

        return execution;
    }
}
