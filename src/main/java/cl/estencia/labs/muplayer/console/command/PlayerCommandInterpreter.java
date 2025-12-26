package cl.estencia.labs.muplayer.console.command;

import cl.estencia.labs.ebot.bus.MessageBus;
import cl.estencia.labs.ebot.bus.exception.BusException;
import cl.estencia.labs.muplayer.audio.model.Album;
import cl.estencia.labs.muplayer.audio.model.Artist;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.bus.MuPlayerBusUtil;
import cl.estencia.labs.muplayer.bus.message.Messages;
import cl.estencia.labs.muplayer.bus.model.MuPlayerResponse;
import cl.estencia.labs.muplayer.bus.model.SkipData;
import cl.estencia.labs.muplayer.config.model.ConsoleCodesData;
import cl.estencia.labs.muplayer.config.reader.ConsoleCodesReader;
import cl.estencia.labs.muplayer.console.common.ConsoleMessages;
import cl.estencia.labs.muplayer.console.common.enums.ConsoleOrderCode;
import cl.estencia.labs.muplayer.console.model.ConsoleOutput;
import cl.estencia.labs.muplayer.console.model.ConsoleImage;
import cl.estencia.labs.muplayer.console.runner.ConsoleRunner;
import cl.estencia.labs.muplayer.console.runner.DaemonRunner;
import cl.estencia.labs.muplayer.console.runner.LocalRunner;
import cl.estencia.labs.muplayer.console.runner.RunnerMode;
import cl.estencia.labs.aucom.core.util.ProcessManager;
import cl.estencia.labs.muplayer.core.cache.CacheManager;
import cl.estencia.labs.muplayer.core.common.enums.SeekOption;
import cl.estencia.labs.muplayer.core.service.LogService;
import cl.estencia.labs.muplayer.core.service.impl.LogServiceImpl;
import cl.estencia.labs.muplayer.core.system.SysInfo;
import cl.estencia.labs.muplayer.core.thread.TaskRunner;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import cl.estencia.labs.muplayer.core.util.ConsolePainter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.orangelogger.sys.Logger;
import org.orangelogger.sys.SystemUtil;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static cl.estencia.labs.muplayer.console.common.enums.OutputType.*;
import static cl.estencia.labs.muplayer.core.cache.CacheVar.RUNNER;
import static cl.estencia.labs.muplayer.core.common.enums.SeekOption.NEXT;
import static cl.estencia.labs.muplayer.core.common.enums.SeekOption.PREV;
import static java.nio.file.StandardOpenOption.WRITE;

@Slf4j
public class PlayerCommandInterpreter implements CommandInterpreter {
    private final Player player;
    private final File playerFolder;

    @Getter
    @Setter
    private boolean on;

    private final CacheManager globalCacheManager;
    private final ConsoleCodesReader consoleCodesReader;
    private final LogService logService;
    private volatile MessageBus messageBus;
    private final AtomicReference<MuPlayerResponse> playerCurrentData;

    private static final String CMD_DIVISOR = " && ";

    public PlayerCommandInterpreter(Player player) {
        this.player = player;
        this.playerFolder = player.getRootFolder();
        this.globalCacheManager = CacheManager.getGlobalCache();
        this.consoleCodesReader = ConsoleCodesReader.getInstance();
        this.logService = new LogServiceImpl();
        this.messageBus = MuPlayerBusUtil.getMessageBus();
        this.playerCurrentData = new AtomicReference<>();
    }

    private boolean isPlayerOn() {
        return player != null && player.isAlive();
    }

    private void execSysCommand(String cmd) {
        try {
            String output = ProcessManager.execute(cmd);
            ProcessManager.writeProcessOutputTo(output, SystemUtil.getStdout());
        } catch (IOException | InterruptedException e) {
            Logger.getLogger(this, e.getMessage()).error();
        }
    }

    private void printTracks(ConsoleOutput execution) {
        if (player == null) {
            return;
        }

        final File rootFolder = player.getRootFolder();
        final List<Track> listTracks = player.getTracks();
        final Track current = playerCurrentData.get().getCurrentTrack();

        execution.append("------------------------------", info);
        if (rootFolder == null) {
            execution.append("Music in folder", info);
        } else {
            execution.append("Music in folder " + rootFolder.getName(), info);
        }
        execution.append("------------------------------", info);

        if (rootFolder != null) {
            Track track;
            File fileTrack;
            for (int i = 0; i < player.getSongsCount(); i++) {
                track = listTracks.get(i);
                fileTrack = track.getDataSource();
                if (current != null && fileTrack.getPath().equals((current.getDataSource()).getPath())) {
                    execution.append("Track " + (i + 1) + ": "
                            + fileTrack.getName(), warn);
                } else {
                    execution.append("Track " + (i + 1) + ": "
                            + fileTrack.getName(), info);
                }
            }
            execution.append("------------------------------", info);
        }
    }

    private void printDetailedTracks(ConsoleOutput execution) {
        final File rootFolder = player.getRootFolder();
        final List<Track> listTracks = player.getTracks();
        final Track current = playerCurrentData.get().getCurrentTrack();
        final Map<Object, Object> mapValues = new HashMap<>();

        if (rootFolder == null) {
            execution.append("Music in folder", info);
        } else {
            execution.append("Music in folder " + rootFolder.getName(), info);
        }
        execution.append("------------------------------", info);

        if (rootFolder != null) {
            File trackFile;
            File trackFolder, prevTrackFolder = null;
            for (int i = 0; i < player.getSongsCount(); i++) {
                trackFile = listTracks.get(i).getDataSource();
                trackFolder = trackFile.getParentFile();
                if (prevTrackFolder == null || !trackFolder.getPath().equals(prevTrackFolder.getPath())) {
                    execution.append((prevTrackFolder != null
                            ? "----------------------------------------------------------------------" +
                            "\n\n----------------------------------------------------------------------" +
                            "\nFolder: "
                            : "----------------------------------------------------------------------\n"
                            + "Folder: ") + trackFolder.getName(), info);
                }
                if (current != null && trackFile.getPath().equals(current.getDataSource().getPath())) {
                    execution.append("\tTrack " + (i + 1) + ": "
                            + trackFile.getName(), warn);
                } else {
                    execution.append("\tTrack " + (i + 1) + ": "
                            + trackFile.getName(), info);
                }

                prevTrackFolder = trackFile.getParentFile();
            }
            execution.append("------------------------------", info);
        }
    }

    private synchronized void printFolderTracks(ConsoleOutput execution) {
        if (player == null) {
            return;
        }

        final List<Track> listTracks = player.getTracks();
        final Track current = playerCurrentData.get().getCurrentTrack();
        final int songsCount = player.getSongsCount();

        File parentFolder = current == null ? null : current.getDataSource().getParentFile();

        execution.append("------------------------------", info);
        if (parentFolder == null) {
            execution.append("Music in current folder", info);
        } else {
            execution.append("Music in folder " + parentFolder.getName(), info);
        }
        execution.append("------------------------------", info);

        if (parentFolder != null) {
            File fileTrack;
            File currentFile = current.getDataSource();

            for (int i = 0; i < songsCount; i++) {
                fileTrack = listTracks.get(i).getDataSource();
                if (fileTrack.getParentFile().equals(parentFolder)) {
                    if (fileTrack.getPath().equals(currentFile.getPath())) {
                        execution.append("Track " + (i + 1) + ": "
                                + fileTrack.getName(), warn);
                    } else {
                        execution.append("Track " + (i + 1) + ": "
                                + fileTrack.getName(), info);
                    }
                }
            }
            execution.append("------------------------------", info);
        }
    }

    private synchronized void printFolderTracks(ConsoleOutput execution, int index) {
        final AtomicReference<Track> current = player.getCurrentTrack();
        final File folder = player.getListFolders().get(index - 1);
        final File currentFile = current.get() != null
                ? current.get().getDataSource()
                : null;

        execution.append("------------------------------", info);

        if (folder != null) {
            execution.append("Music in folder " + folder.getName(), info);
            execution.append("------------------------------", info);

            final AtomicInteger counter = new AtomicInteger(1);
            player.getTracks().stream().filter(track -> track.getDataSource().getParent()
                    .equals(folder.getPath())).forEach(track -> {
                File fileTrack = track.getDataSource();
                if (fileTrack.getParentFile().equals(folder)) {
                    if (fileTrack.getPath().equals(currentFile.getPath())) {
                        execution.append("Track " + (counter.getAndIncrement()) + ": "
                                + fileTrack.getName(), warn);
                    } else {
                        execution.append("Track " + (counter.getAndIncrement()) + ": "
                                + fileTrack.getName(), info);
                    }
                }
            });
            execution.append("------------------------------", info);
        }
    }

    private synchronized void printFolders(ConsoleOutput execution) {
        final File rootFolder = player.getRootFolder();
        final List<String> listFolderPaths = player.getListFolders()
                .stream().map(File::getPath).toList();
        final Track current = playerCurrentData.get().getCurrentTrack();

        execution.append("------------------------------", info);
        if (rootFolder == null) {
            execution.append("Folders", info);
        } else {
            execution.append("Folders in " + rootFolder.getName(), info);
        }
        execution.append("------------------------------", info);

        if (current == null) {
            return;
        }

        File currentTrackFile = current.getDataSource();
        File folder;

        for (int i = 0; i < player.getFoldersCount(); i++) {
            folder = new File(listFolderPaths.get(i));
            if (folder.getPath().equals(currentTrackFile.getParentFile().getPath())) {
                execution.append("Folder " + (i + 1) + ": "
                        + folder.getName(), warn);
            } else {
                execution.append("Folder " + (i + 1) + ": "
                        + folder.getName(), info);
            }

        }
        execution.append("------------------------------", info);
    }

    protected void clearConsole() throws IOException {
        try {
            String clearProcOutput = ProcessManager.execute(SysInfo.IS_UNIX ? "clear" : "cls");
            ProcessManager.writeProcessOutputTo(clearProcOutput, SystemUtil.getStdout());
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    protected void printHelp(Command cmd, ConsoleOutput execution) {
        final String keyValueSeparator = ":\n\t";
        final String helpElementSeparator = "\n\n";
        final String orderSelementsSeparator = ",";

        var consoleCodesDataMap = consoleCodesReader.getJsonSource().getData();
        String helpInfoData;
        if (cmd.hasOptions()) {
            List<String> optionsAsList = cmd.getOptionsAsList();

            helpInfoData = consoleCodesDataMap.parallelStream()
                    .filter(consoleCodesData ->
                            optionsAsList.stream().anyMatch(consoleCodesData::hasOrder))
                    .sorted(Comparator.comparing(ConsoleCodesData::getCode))
                    .sequential()
                    .map(consoleCodesData -> consoleCodesData.getJoinedOrders(orderSelementsSeparator)
                            + keyValueSeparator
                            + consoleCodesData.getHelpInfo())
                    .collect(Collectors.joining(helpElementSeparator));
        } else {
            helpInfoData = consoleCodesDataMap.parallelStream()
                    .sorted(Comparator.comparing(ConsoleCodesData::getCode))
                    .sequential()
                    .map(consoleCodesData -> consoleCodesData.getJoinedOrders(orderSelementsSeparator)
                            + keyValueSeparator
                            + consoleCodesData.getHelpInfo())
                    .collect(Collectors.joining(helpElementSeparator));
        }

        execution.append("---------", info);
        execution.append("Help Info", info);
        execution.append("---------", info);
        execution.append(helpInfoData, info);
    }

    public void showSongInfo(Track track) {
        if (track == null) {
            Logger.getLogger(this, "Current track unavailable").rawError();
        } else {
            Logger.getLogger(this, ConsolePainter.getSongInfo(track)).rawWarning();
        }
    }

    public ConsoleOutput executeCommand(String cmdString) throws Exception {
        if (cmdString.contains(CMD_DIVISOR)) {
            final String[] cmdSplit = cmdString.split(CMD_DIVISOR);
            final List<String> listExec = CollectionUtil.newLinkedList();
            ConsoleOutput exec;

            for (int i = 0; i < cmdSplit.length; i++) {
                exec = executeCommand(new Command(cmdSplit[i].trim()));
                if (exec != null) {
                    listExec.add(exec.getOutputMsg());
                }
            }

            ConsoleOutput consoleOutput = new ConsoleOutput(cmdString);
            consoleOutput.append(listExec.get(listExec.size() - 1), null);
            return consoleOutput;
        } else {
            return executeCommand(new Command(cmdString));
        }
    }

    private void changeOrSkipTrack(Command cmd, ConsoleOutput execution, SeekOption seekOption) throws BusException {
        if (!player.isAlive()){
            return;
        }
        
        if (cmd.hasOptions()) {
            Number skipCount = cmd.getOptionAsNumber(0);
            if (skipCount == null) {
                execution.append("Jump value incorrect", error);
            } else {
                messageBus.publish(Messages.skipTracks(skipCount.intValue(), seekOption));
            }
        } else {
            messageBus.publish(seekOption == NEXT ? Messages.playNext() : Messages.playPrev());
        }
    }

    @Override
    public ConsoleOutput executeCommand(Command cmd) throws Exception {
        final String cmdOrder = cmd.getOrder();
        final ConsoleOrderCode consoleOrderCode = consoleCodesReader.getConsoleOrderCodeByCmdOrder(cmdOrder);
        final ConsoleOutput consoleOutput = new ConsoleOutput(cmd);
        // imprimir output de este objeto no mas

        if (consoleOrderCode == null) {
            consoleOutput.append("Comando desconocido, inserte el comando \"h\" o \"help\"\n" +
                    "para desplegar el menú de ayuda.", warn);
            return consoleOutput;
        }

        switch (consoleOrderCode) {
            case st -> {
//                    synchronized (player != null ? player : null)
                if (player.isAlive()) {
                    messageBus.publish(Messages.reload());
                } else {
                    player.removeAllResponseListeners();
                    player.addResponseListener(muPlayerResponse -> {
                        playerCurrentData.set(muPlayerResponse);
                        showSongInfo(muPlayerResponse.getCurrentTrack());
                    });
                    player.start();

                    messageBus.publish(Messages.start());
                }
            }
            case ist -> consoleOutput.append(isPlayerOn() ? "Is playing" : "Is not playing", warn);
            case pl -> {
                if (player.isAlive()) {
                    if (cmd.hasOptions()) {
                        Number playIndex = cmd.getOptionAsNumber(0);
                        if (playIndex != null && playIndex.intValue() > 0 && playIndex.intValue() <= player.getSongsCount()) {
                            messageBus.publish(Messages.playIndex(playIndex.intValue() - 1));
                        }
                    } else {
                        messageBus.publish(Messages.play());
                    }
                }
            }
            case ps -> {
                if (player.isAlive()) {
                    messageBus.publish(Messages.pause());
                }
            }
            case r -> {
                if (player.isAlive()) {
                    messageBus.publish(Messages.resume());
                }
            }
            case s -> {
                if (player.isAlive()) {
                    messageBus.publish(Messages.stop());
                }
            }
            case n -> changeOrSkipTrack(cmd, consoleOutput, NEXT);
            case p -> changeOrSkipTrack(cmd, consoleOutput, PREV);
            case m -> {
                if (player.isAlive()) {
                    messageBus.publish(Messages.mute());
                }
            }
            case um -> {
                if (player.isAlive()) {
                    messageBus.publish(Messages.unmute());
                }
            }
            case l -> printTracks(consoleOutput);
            case lc -> printFolderTracks(consoleOutput);
            case lf -> {
                if (isPlayerOn()) {
                    if (cmd.hasOptions()) {
                        try {
                            Number index = cmd.getOptionAsNumber(0);
                            printFolderTracks(consoleOutput, index.intValue());
                        } catch (NumberFormatException e) {
                        }
                    } else {
                        printFolders(consoleOutput);
                    }
                }
            }
            case ld -> {
                if (isPlayerOn()) {
                    printDetailedTracks(consoleOutput);
                }
            }
            case gv -> {
                if (player.isAlive()) {
                    consoleOutput.append("Player Volume(0-100): " + player.getVolume(), warn);
                }
            }
            case v -> {
                if (player.isAlive() && cmd.hasOptions()) {
                    Number volume = cmd.getOptionAsNumber(0);
                    if (volume == null) {
                        consoleOutput.append("Volume value incorrect", error);
                    } else {
                        player.setVolume(volume.floatValue());
                        consoleOutput.append("Volume value changed", warn);
                    }
                }
            }
            case gsv -> {
                if (player.isAlive()) {
                    consoleOutput.append("Player Volume(0-100): " + player.getSystemVolume(), warn);
                }
            }
            case sv -> {
                if (player.isAlive() && cmd.hasOptions()) {
                    Number volume = cmd.getOptionAsNumber(0);
                    if (volume == null) {
                        consoleOutput.append("Volume value incorrect", error);
                    } else {
                        player.setSystemVolume(volume.floatValue());
                        consoleOutput.append("Volume value changed", warn);
                    }
                }
            }
            case sh -> {
                messageBus.publish(Messages.shutdown());
                on = false;
            }
            case sk -> {
                if (isPlayerOn() && cmd.hasOptions()) {
                    Number seekSec = cmd.getOptionAsNumber(0);
                    if (seekSec == null) {
                        consoleOutput.append("Seek value incorrect", error);
                    } else {
                        player.seek(seekSec.doubleValue());
                    }
                }
            }
            case skf -> {
                SkipData skipData = new SkipData();
                if (!player.isAlive()) {
                    return consoleOutput;
                }
                
                if (cmd.hasOptions()) {
                    String optionParam = cmd.getOptionAt(0);
                    SeekOption seekOption = optionParam.equalsIgnoreCase(NEXT.name())
                            ? NEXT
                            : (optionParam.equalsIgnoreCase(PREV.name())
                            ? PREV : null);
                    if (cmd.getOptionsCount() >= 2) {
                        if (seekOption != null) {
                            skipData.setSeekOption(seekOption);

                            Number seekFolderCount = cmd.getOptionAsNumber(1);
                            skipData.setSkipCount(seekFolderCount != null
                                    ? seekFolderCount.intValue() : 1);
                        } else {
                            consoleOutput.append("Seek option value incorrect",
                                    error);
                            skipData = null;
                        }
                    } else {
                        if (seekOption != null) {
                            skipData.setSeekOption(seekOption);
                        } else {
                            Number seekFolderCount = cmd.getOptionAsNumber(0);
                            if (seekFolderCount == null) {
                                consoleOutput.append("Seek count value incorrect",
                                        error);
                                skipData = null;
                            } else {
                                skipData.setSkipCount(seekFolderCount.intValue());
                            }
                        }
                    }
                }

                if (skipData != null) {
                    messageBus.publish(Messages.seekFolder(skipData));
                }
            }
            case u -> {
                if (player.isAlive()) {
                    player.reload();
                }
            }
            case g -> {
                if (player.isAlive() && cmd.hasOptions()) {
                    Number gotoSec = cmd.getOptionAsNumber(0);
                    if (gotoSec == null) {
                        consoleOutput.append("Go to value incorrect", error);
                    } else {
                        player.gotoSecond(gotoSec.doubleValue());
                    }
                }
            }
            case c -> {
                if (player.isAlive()) {
                    consoleOutput.append(player.getSongsCount(), info);
                }
            }
            case d -> {
                if (player.isAlive()) {
                    String formattedDuration = playerCurrentData.get().getCurrentTrack() != null ? playerCurrentData.get().getCurrentTrack().getFormattedDuration() : "";
                    consoleOutput.append(formattedDuration, info);
                }
            }
            case cover -> {
                if (player.isAlive()) {
                    if (playerCurrentData.get().getCurrentTrack() == null) {
                        consoleOutput.append("Current track unavailable", error);
                    }
                    else if (cmd.hasOptions()) {
                        final byte[] coverData = playerCurrentData.get().getCurrentTrack().getCoverData();
                        if (coverData != null) {
                            File folderPath = new File(cmd.getOptionAt(0));
                            if (!folderPath.exists()) {
                                folderPath = player.getRootFolder();
                            }
                            File fileCover = new File(folderPath, "cover-" + playerCurrentData.get().getCurrentTrack().getTitle() + ".png");
                            fileCover.createNewFile();
                            Files.write(fileCover.toPath(), coverData, WRITE);
                            consoleOutput.append("Created cover with name " + fileCover.getName(), warn);
                        } else {
                            consoleOutput.append("Current song don't have cover", error);
                        }
                    } else {
                        final byte[] coverData = playerCurrentData.get().getCurrentTrack().getCoverData();
                        final String consoleMessage;

                        if (coverData != null) {
                            final InputStream coverStream = new ByteArrayInputStream(coverData);
                            final ConsoleImage consoleImage = new ConsoleImage(coverStream);

                            consoleMessage = consoleImage.toConsoleString();
                        } else {
                            consoleMessage = ConsoleMessages.NO_COVER_MESSAGE;
                        }

                        consoleOutput.append(consoleMessage, info);
                    }
                }
            }
            case info -> {
                if (player.isAlive()) {
                    if (playerCurrentData.get() != null) {
                        showSongInfo(playerCurrentData.get().getCurrentTrack());
                    } else {
                        consoleOutput.append("No current track available", warn);
                    }
                } else {
                    consoleOutput.append("No current track available, player not initialized", warn);
                }
            }

            case prog -> {
                if (!player.isAlive() || playerCurrentData.get() == null) {
                    consoleOutput.append("Current track unavailable", error);
                } else {
                    final String formattedProgress = playerCurrentData.get().getCurrentTrack().getFormattedProgress();
                    final String formattedDuration = playerCurrentData.get().getCurrentTrack().getFormattedDuration();
                    consoleOutput.append(formattedProgress + "/" + formattedDuration, warn);
                }
            }
            case format -> {
                if (!player.isAlive() || playerCurrentData.get().getCurrentTrack() == null) {
                    consoleOutput.append("Current track unavailable", error);
                } else {
                    final String className = playerCurrentData.get().getCurrentTrack().getClass().getSimpleName();
                    consoleOutput.append(className.substring(0, className.length() - 5).toLowerCase(), warn);
                }
            }
            case title -> {
                if (!player.isAlive() || playerCurrentData.get().getCurrentTrack() == null) {
                    consoleOutput.append("Current track unavailable", error);
                } else {
                    consoleOutput.append(playerCurrentData.get().getCurrentTrack().getTitle(), warn);
                }
            }
            case name -> {
                if (!player.isAlive() || playerCurrentData.get().getCurrentTrack() == null) {
                    consoleOutput.append("Current track unavailable", error);
                } else {
                    consoleOutput.append(playerCurrentData.get().getCurrentTrack().getDataSource().getName(), warn);
                }
            }
            case h -> printHelp(cmd, consoleOutput);
            case sys -> {
                if (cmd.hasOptions()) {
                    execSysCommand(cmd.getOptionsAsString());
                }
            }
            case cls -> clearConsole();
            case pwd -> {
                consoleOutput.append("Current working directory: "
                        + new File(".").getCanonicalPath(), info);
            }
            case sn -> {
                if (isPlayerOn()) {
                    consoleOutput.append(ConsolePainter.getSongInfo(player.getNext()), warn);
                }
            }
            case sp -> {
                if (isPlayerOn()) {
                    consoleOutput.append(ConsolePainter.getSongInfo(player.getPrevious()), warn);
                }
            }
            case pf -> {
                if (isPlayerOn() && cmd.hasOptions()) {
                    final Number fldIndex = cmd.getOptionAsNumber(0);
                    if (fldIndex != null && fldIndex.intValue() > 0) {
                        player.playFolder(fldIndex.intValue() - 1);
                        showSongInfo(playerCurrentData.get().getCurrentTrack());
                    }
                }
            }
//           s
            case arts -> {
                if (player.isAlive() && player.hasSounds()) {
                    final List<Artist> listArtists = player.getArtists();
                    for (Artist artist : listArtists) {
                        consoleOutput.append(artist.getName(), info);
                    }
                    consoleOutput.append("------------------------------", info);
                    consoleOutput.append("Total: " + listArtists.size(), info);
                } else {
                    consoleOutput.append("The player is not active", warn);
                }
            }
            case albs -> {
                if (player.isAlive() && player.hasSounds()) {
                    List<Album> listAlbums = player.getAlbums();
                    for (Album album : listAlbums) {
                        consoleOutput.append(album.getName(), info);
                    }
                    consoleOutput.append("------------------------------", info);
                    consoleOutput.append("Total: " + listAlbums.size(), info);
                } else {
                    consoleOutput.append("The player is not active", warn);
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
                            consoleOutput.append("MuPlayer changed from DAEMON to LOCAL mode!", info);
                        } else {
                            consoleOutput.append("MuPlayer already working with LOCAL mode", warn);
                        }
                    } else if (firstOpt.equalsIgnoreCase(RunnerMode.DAEMON.name())) {
                        if (consoleRunner instanceof LocalRunner) {
                            final DaemonRunner daemonRunner = new DaemonRunner(player);
                            TaskRunner.execute(daemonRunner, daemonRunner.getClass().getSimpleName());
                            globalCacheManager.saveValue(RUNNER, daemonRunner);
                            ((LocalRunner) consoleRunner).shutdown();
                            consoleOutput.append("MuPlayer changed from LOCAL to DAEMON mode!", info);
                        } else {
                            consoleOutput.append("MuPlayer already working with DAEMON mode", warn);
                        }
                    } else {
                        consoleOutput.append("Option selected unknown, the options must be LOCAL or DAEMON", warn);
                    }
                } else {
                    consoleOutput.append("No options selected, the options must be LOCAL or DAEMON", warn);
                }
            }
            case find -> {
                if (!player.isAlive()) {
                    consoleOutput.append("MuPlayer not started yet!", warn);
                } else if (!cmd.hasOptions()) {
                    consoleOutput.append("Search filters not found!", warn);
                } else {
                    final String searchFilter = cmd.getOptionsAsString();
                    final List<String> listResults = CollectionUtil.newFastList(10);

                    player.getTracks().parallelStream()
                            .filter(track -> track.getTitle().toLowerCase()
                                    .contains(searchFilter.toLowerCase()))
                            .forEachOrdered(track -> listResults.add("Track: " + track.getTitle()));

                    player.getListFolders().parallelStream()
                            .filter(folder -> folder.getName().toLowerCase()
                                    .contains(searchFilter.toLowerCase()))
                            .forEachOrdered(folder -> listResults.add("Folder: " + folder.getName()));

                    consoleOutput.append("Search results list", info);
                    consoleOutput.append("--------------------------------------------", info);
                    listResults.parallelStream().sorted()
                            .forEachOrdered(result -> consoleOutput.append(result, info));
                    consoleOutput.append("--------------------------------------------", info);
                }


            }
//            case smf -> {
//                if (cmd.hasNotOptions()) {
//                    logService.errorLog("[Set music folder]\nCommand use: \n\tsmf ${music-folder-path}\n");
//                } else {
//                    String musicFolderPath = cmd.getOptionAt(0);
//                    File musicFolderFile = new File(musicFolderPath);
//                    if (!musicFolderFile.exists()) {
//                        logService.errorLog("[" + musicFolderFile + "] doesn't exist\n");
//                    } else if (!musicFolderFile.isDirectory()) {
//                        logService.errorLog("[" + musicFolderFile + "] is not a folder\n");
//                    } else {
//                        player.addMusic(musicFolderFile);
//                    }
//                }
//            }
        }

        return consoleOutput;
    }
}
