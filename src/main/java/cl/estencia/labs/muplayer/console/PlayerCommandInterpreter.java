package cl.estencia.labs.muplayer.console;

import cl.estencia.labs.muplayer.audio.common.enums.SeekOption;
import cl.estencia.labs.muplayer.audio.model.Album;
import cl.estencia.labs.muplayer.audio.model.Artist;
import cl.estencia.labs.muplayer.audio.player.MusicPlayer;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.data.Cover;
import cl.estencia.labs.muplayer.console.command.Command;
import cl.estencia.labs.muplayer.console.command.CommandInterpreter;
import cl.estencia.labs.muplayer.console.common.enums.ConsoleOrderCode;
import cl.estencia.labs.muplayer.console.common.enums.OutputLevel;
import cl.estencia.labs.muplayer.console.model.ConsoleOutput;
import cl.estencia.labs.muplayer.console.model.table.Alignment;
import cl.estencia.labs.muplayer.console.runner.ConsoleRunner;
import cl.estencia.labs.muplayer.console.runner.DaemonRunner;
import cl.estencia.labs.muplayer.console.runner.LocalRunner;
import cl.estencia.labs.muplayer.console.runner.RunnerMode;
import cl.estencia.labs.muplayer.core.bus.message.Events;
import cl.estencia.labs.muplayer.core.bus.model.LoadingPlayerInfo;
import cl.estencia.labs.muplayer.core.bus.model.PlayerInfo;
import cl.estencia.labs.muplayer.core.bus.model.SkipData;
import cl.estencia.labs.muplayer.core.thread.TaskRunner;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cl.estencia.labs.muplayer.audio.common.enums.SeekOption.NEXT;
import static cl.estencia.labs.muplayer.audio.common.enums.SeekOption.PREV;
import static cl.estencia.labs.muplayer.config.reader.ResourceReaders.CONSOLE_CODES_READER;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleChars.CART_RETURN;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.LINE_BREAK_CHAR;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.SANDGLASS;
import static cl.estencia.labs.muplayer.console.common.enums.ConsoleOutputMode.CLEAN;
import static cl.estencia.labs.muplayer.console.common.enums.ConsoleOutputMode.DEFAULT;
import static cl.estencia.labs.muplayer.console.common.enums.OutputLevel.*;
import static cl.estencia.labs.muplayer.console.util.PlayerInterpreterUtil.*;
import static cl.estencia.labs.muplayer.console.util.SystemCommandExecutor.clearConsole;
import static cl.estencia.labs.muplayer.core.bus.message.PlayerEventTopics.LOADING;
import static cl.estencia.labs.muplayer.core.bus.message.PlayerEventTopics.SHUTDOWN;
import static cl.estencia.labs.muplayer.core.cache.CacheManager.CACHE;
import static cl.estencia.labs.muplayer.core.cache.CacheVar.RUNNER;
import static cl.estencia.labs.muplayer.core.log.ConsolePrinter.print;

@Slf4j
public class PlayerCommandInterpreter implements CommandInterpreter {
    private final MusicPlayer player;

    @Getter
    @Setter
    private volatile boolean on;

    private final AtomicReference<PlayerInfo> playerInfo;
    public PlayerCommandInterpreter(MusicPlayer player) {
        this.player = player;
        this.playerInfo = new AtomicReference<>();
    }

    private boolean isPlayerOn() {
        return player != null && player.isAlive();
    }

    @Override
    public ConsoleOutput execute(Command cmd) throws Exception {
        final String cmdOrder = cmd.getOrder();
        final ConsoleOrderCode consoleOrderCode = CONSOLE_CODES_READER.getConsoleOrderCodeByCmdOrder(cmdOrder);
        final ConsoleOutput consoleOutput = new ConsoleOutput(cmd);
        // imprimir output de este objeto no mas

        if (consoleOrderCode == null) {
            consoleOutput.append("Comando desconocido, inserte el comando \"h\" o \"help\"\n" +
                    "para desplegar el menú de ayuda.", warn);
            return consoleOutput;
        }

        switch (consoleOrderCode) {
            case st -> {
                if (player.isAlive()) {
                    player.sendEvent(Events.reload());
                } else {
                    // TODO: ojo con este remove cuando inicialice mpris antes del command interpreter,
                    // puede que me pitee los listener de mpris
                    player.removeAllResponseListeners();
                    player.addResponseListener(playerInfoResp -> {
                        synchronized (playerInfo) {
                            playerInfo.set(playerInfoResp);
                        }

                        printConsoleInfo(player, DEFAULT, true);
                    });

                    player.addListener(SHUTDOWN, message -> {
                        CACHE.clear();
                        System.exit(0);
                    });

                    player.addListener(LOADING, message -> {
                        LoadingPlayerInfo loadingPlayerInfo = message.getData(LoadingPlayerInfo.class);
                        if (loadingPlayerInfo == null) {
                            return;
                        }

                        print(String.valueOf(CART_RETURN) + SANDGLASS
                                + " Loading " + loadingPlayerInfo.tracksCount() + " tracks...");
                    });

                    player.sendEvent(Events.start());
                }
            }
            case ist -> consoleOutput.append(isPlayerOn() ? "Is playing" : "Is not playing", warn);
            case pl -> {
                if (player.isAlive()) {
                    if (cmd.hasOptions()) {
                        Number playIndex = cmd.getOptionAsNumber(0);
                        if (playIndex != null && playIndex.intValue() > 0 && playIndex.intValue() <= player.getTracksCount()) {
                            player.sendEvent(Events.playIndex(playIndex.intValue() - 1));
                        }
                    } else {
                        player.sendEvent(Events.play());
                    }
                }
            }
            case ps -> {
                if (player.isAlive()) {
                    player.sendEvent(Events.pause());
                }
            }
            case r -> {
                if (player.isAlive()) {
                    player.sendEvent(Events.resume());
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
                    player.sendEvent(Events.stop());
                }
            }
            case n -> changeOrSkipTrack(player, cmd, consoleOutput, NEXT);
            case p -> changeOrSkipTrack(player, cmd, consoleOutput, PREV);
            case m -> {
                if (player.isAlive()) {
                    PlayerInfo playerResponse = playerInfo.get();
                    if (playerResponse == null || playerResponse.getPlayerStatusData() == null) {
                        return consoleOutput;
                    }

                    if (playerResponse.getPlayerStatusData().isMute()) {
                        player.sendEvent(Events.unmute());
                    } else {
                        player.sendEvent(Events.mute());
                    }
                }
            }
            case l -> {
                Alignment alignment;
                if (cmd.hasOptions() && Alignment.isValidName(cmd.getOptionAt(0))) {
                    alignment = Alignment.fromName(cmd.getOptionAt(0));
                } else {
                    alignment = Alignment.CENTER;
                }

                printTracks(player, playerInfo, consoleOutput, alignment);
            }
            case lc -> {
                Alignment alignment;
                if (cmd.hasOptions() && Alignment.isValidName(cmd.getOptionAt(0))) {
                    alignment = Alignment.fromName(cmd.getOptionAt(0));
                } else {
                    alignment = Alignment.CENTER;
                }

                printFolderTracks(player, playerInfo, consoleOutput, alignment);
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
                        printFolders(player, playerInfo, consoleOutput);
                    }
                }
            }
            case ld -> {
                if (isPlayerOn()) {
                    printDetailedTracks(player, playerInfo, consoleOutput);
                }
            }
            case gv -> printPlayerVolume(player, consoleOutput, false);
            case v -> changePlayerVolume(player, cmd, false);
            case gsv -> printPlayerVolume(player, consoleOutput, true);
            case sv -> changePlayerVolume(player, cmd, true);
            case sh -> {
                if (player.isAlive()) {
                    player.sendEvent(Events.shutdown());
                }

                on = false;
            }
            case sk -> {
                if (isPlayerOn() && cmd.hasOptions()) {
                    Number seconds = cmd.getOptionAsNumber(0);
                    if (seconds == null) {
                        consoleOutput.append("Seek value incorrect", error);
                    } else {
                        player.sendEvent(Events.seekSeconds(seconds.intValue()));
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
                    player.sendEvent(Events.seekFolder(skipData));
                }
            }
            case u -> {
                if (player.isAlive()) {
                    player.sendEvent(Events.reload());
                }
            }
            case g -> {
                if (player.isAlive() && cmd.hasOptions()) {
                    Number seconds = cmd.getOptionAsNumber(0);
                    if (seconds == null) {
                        consoleOutput.append("Go to value incorrect", error);
                    } else {
                        player.sendEvent(Events.gotoSeconds(seconds.intValue()));
                    }
                }
            }
            case c -> {
                if (player.isAlive()) {
                    consoleOutput.append(player.getTracksCount(), info);
                }
            }
            case d -> {
                if (player.isAlive()) {
                    String formattedDuration = playerInfo.get().getCurrentTrack() != null ? playerInfo.get().getCurrentTrack().getFormattedDuration() : "";
                    consoleOutput.append(formattedDuration, info);
                }
            }
            case cover -> {
                if (player.isAlive()) {
                    PlayerInfo info = playerInfo.get();
                    Track current = info != null ? info.getCurrentTrack() : null;
                    if (current == null) {
                        consoleOutput.append("Current track unavailable", error);
                    }

                    else if (cmd.hasOptions()) {
                        final Cover cover = current.getCover();
                        final String coverName = cmd.getOptionAt(0).trim();
                        if (cover.hasData()) {
                            File folderPath = new File(cmd.getOptionAt(0));
                            if (!folderPath.exists()) {
                                folderPath = player.getRootFolder();
                            }

                            File coverFile = cover.saveInLocalStorage(folderPath, coverName);
                            consoleOutput.append("Created cover with name " + coverFile.getName(), warn);
                        } else {
                            consoleOutput.append("Current song don't have cover", error);
                        }
                    } else {
                        final Cover cover = info.getCurrentTrack().getCover();

                        consoleOutput.append(LINE_BREAK_CHAR);
                        consoleOutput.append(cover.toString(), OutputLevel.info);
                    }
                }
            }
            case info -> {
                if (player.isAlive()) {
                    if (playerInfo.get() != null && playerInfo.get().getCurrentTrack() != null) {
                        String trackInfo = getTrackInfo(playerInfo.get().getCurrentTrack(), CLEAN);
                        consoleOutput.append(trackInfo);
                    } else {
                        consoleOutput.append("No current track available", warn);
                    }
                } else {
                    consoleOutput.append("No current track available, player not initialized", warn);
                }
            }

            case prog -> {
                if (!player.isAlive() || playerInfo.get() == null) {
                    consoleOutput.append("Current track unavailable", error);
                } else {
                    final String formattedProgress = playerInfo.get().getCurrentTrack().getFormattedProgress();
                    final String formattedDuration = playerInfo.get().getCurrentTrack().getFormattedDuration();
                    consoleOutput.append(formattedProgress + "/" + formattedDuration, warn);
                }
            }
            case format -> {
                if (!player.isAlive() || playerInfo.get().getCurrentTrack() == null) {
                    consoleOutput.append("Current track unavailable", error);
                } else {
                    final String className = playerInfo.get().getCurrentTrack().getClass().getSimpleName();
                    consoleOutput.append(className.substring(0, className.length() - 5).toLowerCase(), warn);
                }
            }
            case title -> {
                if (!player.isAlive() || playerInfo.get().getCurrentTrack() == null) {
                    consoleOutput.append("Current track unavailable", error);
                } else {
                    consoleOutput.append(playerInfo.get().getCurrentTrack().getTitle(), warn);
                }
            }
            case name -> {
                if (!player.isAlive() || playerInfo.get().getCurrentTrack() == null) {
                    consoleOutput.append("Current track unavailable", error);
                } else {
                    consoleOutput.append(playerInfo.get().getCurrentTrack().getDataSource().getName(), warn);
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
                        player.sendEvent(Events.playFolder(fldIndex.intValue() - 1));
                    }
                }
            }
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
                    final ConsoleRunner consoleRunner = CACHE.get(RUNNER);
                    if (firstOpt.equalsIgnoreCase(RunnerMode.LOCAL.name())) {
                        if (consoleRunner instanceof DaemonRunner) {
                            final LocalRunner localRunner = new LocalRunner(player);
                            TaskRunner.execute(localRunner, localRunner.getClass().getSimpleName());
                            CACHE.set(RUNNER, localRunner);
                            ((DaemonRunner) consoleRunner).shutdown();
                            consoleOutput.append("MuPlayer changed from DAEMON to LOCAL mode!", info);
                        } else {
                            consoleOutput.append("MuPlayer already working with LOCAL mode", warn);
                        }
                    } else if (firstOpt.equalsIgnoreCase(RunnerMode.DAEMON.name())) {
                        if (consoleRunner instanceof LocalRunner) {
                            final DaemonRunner daemonRunner = new DaemonRunner(player);
                            TaskRunner.execute(daemonRunner, daemonRunner.getClass().getSimpleName());
                            CACHE.set(RUNNER, daemonRunner);
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
                    final List<String> listResults = CollectionUtil.newMiniList();

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
