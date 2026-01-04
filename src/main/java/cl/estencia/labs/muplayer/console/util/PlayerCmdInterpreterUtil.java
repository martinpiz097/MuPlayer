package cl.estencia.labs.muplayer.console.util;

import cl.estencia.labs.aucom.core.util.ProcessManager;
import cl.estencia.labs.ebot.bus.MessageBus;
import cl.estencia.labs.ebot.bus.exception.BusException;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.bus.MessageBusUtil;
import cl.estencia.labs.muplayer.bus.message.Messages;
import cl.estencia.labs.muplayer.bus.model.MuPlayerResponse;
import cl.estencia.labs.muplayer.config.model.ConsoleCodesData;
import cl.estencia.labs.muplayer.config.reader.ConsoleCodesReader;
import cl.estencia.labs.muplayer.console.command.Command;
import cl.estencia.labs.muplayer.console.model.ConsoleOutput;
import cl.estencia.labs.muplayer.console.model.table.ConsoleTable;
import cl.estencia.labs.muplayer.console.model.table.ConsoleTableRow;
import cl.estencia.labs.muplayer.console.model.table.Padding;
import cl.estencia.labs.muplayer.core.common.enums.SeekOption;
import cl.estencia.labs.muplayer.core.system.SysInfo;
import cl.estencia.labs.muplayer.core.util.ConsolePainter;
import lombok.extern.slf4j.Slf4j;
import org.orangelogger.sys.Logger;
import org.orangelogger.sys.SystemUtil;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static cl.estencia.labs.muplayer.console.common.enums.OutputType.*;
import static cl.estencia.labs.muplayer.core.common.enums.SeekOption.NEXT;

@Slf4j
public class PlayerCmdInterpreterUtil {
    public static void execSysCommand(String cmd) {
        try {
            String output = ProcessManager.execute(cmd);
            ProcessManager.writeProcessOutputTo(output, SystemUtil.getStdout());
        } catch (IOException | InterruptedException e) {
            Logger.getLogger(PlayerCmdInterpreterUtil.class, e.getMessage()).error();
        }
    }

    public static void printTracks(Player player, AtomicReference<MuPlayerResponse> playerCurrentData,
                                   ConsoleOutput consoleOutput) {
        if (player == null) {
            return;
        }

        final Track current = playerCurrentData.get().getCurrentTrack();

        ConsoleTable consoleTable = new ConsoleTable("Tracks List",
                ConsoleUtil.getOutputColor(info),
                new Padding(0, 5, 0, 5),
                false, true);
        consoleTable.addColumn("Title");
        consoleTable.addColumn("Album");
        consoleTable.addColumn("Artist");

        player.getTracks().forEach(track -> {
            String album = track.getAlbum() != null ? track.getAlbum() : "Unknown";
            String artist = track.getArtist() != null ? track.getArtist() : "Unknown";

            String color = track.equals(current)
                    ? ConsoleUtil.getOutputColor(warn)
                    : ConsoleUtil.getOutputColor(info);

            ConsoleTableRow row = new ConsoleTableRow(color);
            row.addCell(track.getTitle());
            row.addCell(album);
            row.addCell(artist);

            consoleTable.addRow(row);
        });

        consoleOutput.append(consoleTable.draw());
    }

    public static void printDetailedTracks(Player player, AtomicReference<MuPlayerResponse> playerCurrentData,
                                           ConsoleOutput execution) {
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

    public static synchronized void printFolderTracks(Player player, AtomicReference<MuPlayerResponse> playerCurrentData,
                                                      ConsoleOutput consoleOutput) {
        if (player == null) {
            return;
        }

        final List<Track> listTracks = player.getTracks();
        final Track current = playerCurrentData.get().getCurrentTrack();
        final int songsCount = player.getSongsCount();

        File parentFolder = current == null ? null : current.getDataSource().getParentFile();

        consoleOutput.append("------------------------------", info);
        if (parentFolder == null) {
            consoleOutput.append("Music in current folder", info);
        } else {
            consoleOutput.append("Music in folder " + parentFolder.getName(), info);
        }
        consoleOutput.append("------------------------------", info);

        if (parentFolder != null) {
            File fileTrack;
            File currentFile = current.getDataSource();

            for (int i = 0; i < songsCount; i++) {
                fileTrack = listTracks.get(i).getDataSource();
                if (fileTrack.getParentFile().equals(parentFolder)) {
                    if (fileTrack.getPath().equals(currentFile.getPath())) {
                        consoleOutput.append("Track " + (i + 1) + ": "
                                + fileTrack.getName(), warn);
                    } else {
                        consoleOutput.append("Track " + (i + 1) + ": "
                                + fileTrack.getName(), info);
                    }
                }
            }
            consoleOutput.append("------------------------------", info);
        }
    }

    public static synchronized void printFolderTracks(Player player, ConsoleOutput execution, int index) {
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

    public static synchronized void printFolders(Player player, AtomicReference<MuPlayerResponse> playerCurrentData,
                                                 ConsoleOutput execution) {
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

    public static void clearConsole() throws IOException {
        try {
            String clearProcOutput = ProcessManager.execute(SysInfo.IS_UNIX ? "clear" : "cls");
            ProcessManager.writeProcessOutputTo(clearProcOutput, SystemUtil.getStdout());
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void printHelp(Command cmd, ConsoleOutput execution) {
        final String keyValueSeparator = ":\n\t";
        final String helpElementSeparator = "\n\n";
        final String orderSelementsSeparator = ",";
        final ConsoleCodesReader consoleCodesReader = ConsoleCodesReader.getInstance();

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

    public static void showSongInfo(Track track) {
        if (track == null) {
            Logger.getLogger(PlayerCmdInterpreterUtil.class, "Current track unavailable").rawError();
        } else {
            Logger.getLogger(PlayerCmdInterpreterUtil.class, ConsolePainter.getSongInfo(track)).rawWarning();
        }
    }

    public static void changeOrSkipTrack(Player player, Command cmd, ConsoleOutput execution, SeekOption seekOption) throws BusException {
        if (!player.isAlive()) {
            return;
        }

        final MessageBus messageBus = MessageBusUtil.getMessageBus();
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

}
