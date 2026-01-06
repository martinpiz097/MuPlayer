package cl.estencia.labs.muplayer.console;

import cl.estencia.labs.ebot.bus.MessageBus;
import cl.estencia.labs.ebot.bus.model.message.Message;
import cl.estencia.labs.ebot.bus.model.pubsub.sub.MessageListener;
import cl.estencia.labs.muplayer.audio.model.Album;
import cl.estencia.labs.muplayer.audio.model.Artist;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.console.model.table.Alignment;
import cl.estencia.labs.muplayer.console.unix.NativeInputReader;
import cl.estencia.labs.muplayer.core.bus.message.MuPlayerTopic;
import cl.estencia.labs.muplayer.core.bus.util.MessageBusUtil;
import cl.estencia.labs.muplayer.core.bus.message.Messages;
import cl.estencia.labs.muplayer.core.bus.model.MuPlayerResponse;
import cl.estencia.labs.muplayer.core.bus.model.SkipData;
import cl.estencia.labs.muplayer.config.reader.ConsoleCodesReader;
import cl.estencia.labs.muplayer.console.command.Command;
import cl.estencia.labs.muplayer.console.command.CommandInterpreter;
import cl.estencia.labs.muplayer.console.common.constants.ConsoleMessages;
import cl.estencia.labs.muplayer.console.common.enums.ConsoleOrderCode;
import cl.estencia.labs.muplayer.console.model.ConsoleOutput;
import cl.estencia.labs.muplayer.console.model.ConsoleImage;
import cl.estencia.labs.muplayer.console.runner.ConsoleRunner;
import cl.estencia.labs.muplayer.console.runner.DaemonRunner;
import cl.estencia.labs.muplayer.console.runner.LocalRunner;
import cl.estencia.labs.muplayer.console.runner.RunnerMode;
import cl.estencia.labs.muplayer.core.cache.CacheManager;
import cl.estencia.labs.muplayer.audio.common.enums.SeekOption;
import cl.estencia.labs.muplayer.core.cache.CacheVar;
import cl.estencia.labs.muplayer.core.service.LogService;
import cl.estencia.labs.muplayer.core.service.impl.LogServiceImpl;
import cl.estencia.labs.muplayer.core.thread.TaskRunner;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static cl.estencia.labs.muplayer.console.common.enums.OutputType.*;
import static cl.estencia.labs.muplayer.console.util.PlayerCmdInterpreterUtil.*;
import static cl.estencia.labs.muplayer.core.cache.CacheVar.RUNNER;
import static cl.estencia.labs.muplayer.audio.common.enums.SeekOption.NEXT;
import static cl.estencia.labs.muplayer.audio.common.enums.SeekOption.PREV;
import static java.nio.file.StandardOpenOption.WRITE;

@Slf4j
public class PlayerCommandInterpreter implements CommandInterpreter {
    private final Player player;

    @Getter
    @Setter
    private volatile boolean on;

    private final CacheManager globalCacheManager;
    private final ConsoleCodesReader consoleCodesReader;
    private final LogService logService;
    private volatile MessageBus messageBus;
    private final AtomicReference<MuPlayerResponse> playerCurrentData;

    private static final String CMD_DIVISOR = " && ";

    public PlayerCommandInterpreter(Player player) {
        this.player = player;
        this.globalCacheManager = CacheManager.getGlobalCache();
        this.consoleCodesReader = ConsoleCodesReader.getInstance();
        this.logService = new LogServiceImpl();
        this.messageBus = MessageBusUtil.getMessageBus();
        this.playerCurrentData = new AtomicReference<>();
    }

    private boolean isPlayerOn() {
        return player != null && player.isAlive();
    }

    @Override
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
                        showTrackInfo(muPlayerResponse.getCurrentTrack(), null);
                    });

                    messageBus.subscribe(MuPlayerTopic.SHUTDOWN.name(),
                            message -> System.exit(0));

//                    player.start();

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
            case root -> {
                if (player.isAlive()) {
                    consoleOutput.append("Root folder path: "
                            + player.getRootFolder().getCanonicalPath(), info);
                }
            }
            case s -> {
                if (player.isAlive()) {
                    messageBus.publish(Messages.stop());
                }
            }
            case n -> changeOrSkipTrack(player, cmd, consoleOutput, NEXT);
            case p -> changeOrSkipTrack(player, cmd, consoleOutput, PREV);
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
            case l -> {
                Alignment alignment;
                if (cmd.hasOptions() && Alignment.isValidName(cmd.getOptionAt(0))) {
                    alignment = Alignment.fromName(cmd.getOptionAt(0));
                } else {
                    alignment = Alignment.CENTER;
                }

                printTracks(player, playerCurrentData, consoleOutput, alignment);
            }
            case lc -> {
                Alignment alignment;
                if (cmd.hasOptions() && Alignment.isValidName(cmd.getOptionAt(0))) {
                    alignment = Alignment.fromName(cmd.getOptionAt(0));
                } else {
                    alignment = Alignment.CENTER;
                }

                printFolderTracks(player, playerCurrentData, consoleOutput, alignment);
            }
            case lf -> {
                if (isPlayerOn()) {
                    if (cmd.hasOptions()) {
                        try {
                            Number number = cmd.getOptionAsNumber(0);
                            printFolderTracks(player, consoleOutput, number.intValue());
                        } catch (NumberFormatException e) {
                        }
                    } else {
                        printFolders(player, playerCurrentData, consoleOutput);
                    }
                }
            }
            case ld -> {
                if (isPlayerOn()) {
                    printDetailedTracks(player, playerCurrentData, consoleOutput);
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
                        messageBus.publish(Messages.setVolume(volume.floatValue()));
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
                if (player.isAlive()) {
                    messageBus.publish(Messages.shutdown());
                }

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
                        showTrackInfo(playerCurrentData.get().getCurrentTrack(), consoleOutput);
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
                    consoleOutput.append(getTrackInfo(player.getNext()), warn);
                }
            }
            case sp -> {
                if (isPlayerOn()) {
                    consoleOutput.append(getTrackInfo(player.getPrevious()));
                }
            }
            case snf -> {
                if (isPlayerOn()) {
                    int nextFolderNumber = player.getNextFolderNumber();
                    if (nextFolderNumber != -1) {
                        String folderName = player.getFolder(nextFolderNumber).getName();
                        consoleOutput.append("Next folder name: " + folderName, info);
                    } else {
                        consoleOutput.append("No folders!", warn);
                    }
                }
            }
            case spf -> {
                if (isPlayerOn()) {
                    int prevFolderNumber = player.getPreviousFolderNumber();
                    if (prevFolderNumber != -1) {
                        String folderName = player.getFolder(prevFolderNumber).getName();
                        consoleOutput.append("Previous folder name: " + folderName, info);
                    } else {
                        consoleOutput.append("No folders!", warn);
                    }
                }
            }
            case pf -> {
                if (isPlayerOn() && cmd.hasOptions()) {
                    final Number fldIndex = cmd.getOptionAsNumber(0);
                    if (fldIndex != null && fldIndex.intValue() > 0) {
                        player.playFolder(fldIndex.intValue() - 1);
                        showTrackInfo(playerCurrentData.get().getCurrentTrack(), consoleOutput);
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
