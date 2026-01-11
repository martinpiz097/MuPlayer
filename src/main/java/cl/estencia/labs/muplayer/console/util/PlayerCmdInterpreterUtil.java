package cl.estencia.labs.muplayer.console.util;

import cl.estencia.labs.aucom.core.util.ProcessManager;
import cl.estencia.labs.ebot.bus.MessageBus;
import cl.estencia.labs.ebot.bus.exception.BusException;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.console.model.table.*;
import cl.estencia.labs.muplayer.core.bus.util.MessageBusUtil;
import cl.estencia.labs.muplayer.core.bus.message.Messages;
import cl.estencia.labs.muplayer.core.bus.model.MuPlayerResponse;
import cl.estencia.labs.muplayer.config.model.ConsoleCodesData;
import cl.estencia.labs.muplayer.config.reader.ConsoleCodesReader;
import cl.estencia.labs.muplayer.console.command.Command;
import cl.estencia.labs.muplayer.console.model.ConsoleOutput;
import cl.estencia.labs.muplayer.audio.common.enums.SeekOption;
import lombok.extern.slf4j.Slf4j;
import org.orangelogger.sys.Logger;
import org.orangelogger.sys.SystemUtil;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static cl.estencia.labs.muplayer.console.command.SystemCommands.CLEAR_CONSOLE_UNIX;
import static cl.estencia.labs.muplayer.console.command.SystemCommands.CLEAR_CONSOLE_WINDOWS;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.SPACE;
import static cl.estencia.labs.muplayer.console.common.enums.OutputType.*;
import static cl.estencia.labs.muplayer.console.util.ConsoleUtil.getOutputColor;
import static cl.estencia.labs.muplayer.audio.common.enums.SeekOption.NEXT;
import static cl.estencia.labs.muplayer.core.system.SysInfo.IS_UNIX;

@Slf4j
public class PlayerCmdInterpreterUtil {
    public static void execSysCommand(String cmd) {
        try {
            String output = ProcessManager.executeLegacy(
                    cmd.split(String.valueOf(SPACE)));
            ProcessManager.writeProcessOutputTo(output, SystemUtil.getStdout());
        } catch (IOException | InterruptedException e) {
            Logger.getLogger(PlayerCmdInterpreterUtil.class, e.getMessage()).error();
        }
    }

    public static void printTracks(Player player, AtomicReference<MuPlayerResponse> playerCurrentData,
                                   ConsoleOutput consoleOutput, Alignment alignment) {
        if (player == null || playerCurrentData.get() == null
                || playerCurrentData.get().getCurrentTrack() == null) {
            return;
        }

        final Track current = playerCurrentData.get().getCurrentTrack();
        final String tableColor = getOutputColor(info);
        final int contentSizeLimit = 100;

        ConsoleTable consoleTable = new ConsoleTable("Tracks List",
                tableColor, alignment,
                new Padding(0, 1, 0, 1),
                false, true, contentSizeLimit);
        consoleTable.addColumns("N°", "Title", "Album", "Artist");

        AtomicInteger indexCounter = new AtomicInteger(0);
        player.getTracks().forEach(track -> {
            String album = track.getAlbum() != null ? track.getAlbum() : "Unknown";
            String artist = track.getArtist() != null ? track.getArtist() : "Unknown";
            String color = getOutputColor(track.equals(current) ? warn : info);

            ConsoleTableRow row = new ConsoleTableRow(color);
            row.addCells(indexCounter.incrementAndGet(),
                    track.getTitle(), album, artist);

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

    public static synchronized void printFolderTracks(Player player, File tracksFolder, AtomicReference<MuPlayerResponse> playerCurrentData,
                                                      ConsoleOutput consoleOutput, Alignment alignment) {
        if (player == null) {
            return;
        }
        if (tracksFolder == null || !tracksFolder.exists()) {
            return;
        }

        final List<Track> listTracks = player.getTracks();
        final int songsCount = player.getSongsCount();

        final String tableColor = getOutputColor(info);
        final int contentSizeLimit = 40;

        final Track currentTrack = playerCurrentData.get().getCurrentTrack();
        if (currentTrack == null) {
            return;
        }

        final File currentTrackFile = currentTrack.getDataSource();

        ConsoleTable consoleTable = new ConsoleTable("Music in folder " + tracksFolder.getName(),
                tableColor,
                alignment,
                new Padding(0, 1, 0, 1),
                false, true, contentSizeLimit);
        consoleTable.addColumns("N°", "Title", "Album", "Artist");

        Track track;
        File fileTrack;
        ConsoleTableRow row;
        String color;
        String album;
        String artist;

        for (int i = 0; i < songsCount; i++) {
            track = listTracks.get(i);
            fileTrack = track.getDataSource();
            if (fileTrack.getParentFile().equals(tracksFolder)) {
                color = ConsoleUtil.getOutputColor(
                        fileTrack.getPath().equals(currentTrackFile.getPath())
                                ? warn : info);
                album = track.getAlbum() != null ? track.getAlbum() : "Unknown";
                artist = track.getArtist() != null ? track.getArtist() : "Unknown";

                row = new ConsoleTableRow(color);
                row.addCells(i + 1, track.getTitle(), album, artist);

                consoleTable.addRow(row);
            }
        }

        consoleOutput.append(consoleTable.draw());
    }

    public static synchronized void printFolderTracks(Player player, AtomicReference<MuPlayerResponse> playerCurrentData,
                                                      ConsoleOutput consoleOutput, Alignment alignment) {
        if (player == null) {
            return;
        }

        final Track currentTrack = playerCurrentData.get().getCurrentTrack();
        if (currentTrack == null) {
            return;
        }

        final File currentTrackFile = currentTrack.getDataSource();
        final File parentFolder = currentTrackFile != null ? currentTrackFile.getParentFile() : null;

        printFolderTracks(player, parentFolder,
                playerCurrentData, consoleOutput, alignment);
    }

    public static synchronized void printFolderTracks(Player player, ConsoleOutput execution, int number) {
        final AtomicReference<Track> currentTrack = player.getCurrentTrack();
        final File folder = player.getFolder(number);
        final File currentFile = currentTrack.get() != null
                ? currentTrack.get().getDataSource()
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

    public static void clearConsole() {
        try {
            String clearProcOutput = ProcessManager.execute(IS_UNIX
                    ? CLEAR_CONSOLE_UNIX : CLEAR_CONSOLE_WINDOWS);
            ProcessManager.writeProcessOutputTo(clearProcOutput, SystemUtil.getStdout());
        } catch (Exception e) {
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

    public static void showTrackInfo(Track track, ConsoleOutput consoleOutput, boolean clearConsole) {
        if (clearConsole) {
            clearConsole();
        }

        if (consoleOutput != null) {
            if (track != null) {
                String trackInfo = getTrackInfo(track);
                consoleOutput.append(trackInfo);
            } else {
                consoleOutput.append("Current track unavailable", error);
            }
        } else {
            if (track != null) {
                Logger.getLogger(PlayerCmdInterpreterUtil.class, getTrackInfo(track)).rawInfo();
            } else {
                Logger.getLogger(PlayerCmdInterpreterUtil.class, "Current track unavailable").rawError();
            }
        }
    }

    public static void showTrackInfo(Track track, boolean clearConsole) {
        showTrackInfo(track, null, clearConsole);
    }

    public static void changeOrSkipTrack(Player player, Command cmd, ConsoleOutput execution, SeekOption seekOption) throws BusException {
        if (!player.isAlive()) {
            return;
        }

        final MessageBus messageBus = MessageBusUtil.getMessageBus();
        if (cmd.hasOptions()) {
            Number skipCount = cmd.getOptionAsNumber(0);
            if (skipCount == null) {
                execution.append("Incorrect skip value ", error);
            } else {
                messageBus.publish(Messages.skipTracks(skipCount.intValue(), seekOption));
            }
        } else {
            messageBus.publish(seekOption == NEXT ? Messages.playNext() : Messages.playPrev());
        }
    }

    public static String getTrackInfo(Track track) {
        String elementsColor = getOutputColor(info);
        ConsoleTable consoleTable = new ConsoleTable(null, elementsColor,
                new Padding(0, 3, 0, 3),
                false, false);

        final String title = track.getTitle();
        final String album = track.getAlbum();
        final String artist = track.getArtist();
        final String year = track.getYear();
        final String duration = track.getFormattedDuration();
        final String genre = track.getGenre();
        final String hasCover = track.hasCover() ? "Yes" : "No";
        final String bitrate = track.getBitrate();

        int contentSize = 100;

        consoleTable.addRowWithCells(new ConsoleTableCell("Title: " + title, elementsColor, contentSize));

        if (album != null) {
            consoleTable.addRowWithCells(new ConsoleTableCell("Album: " + album, elementsColor, contentSize));
        }

        if (artist != null) {
            consoleTable.addRowWithCells(new ConsoleTableCell("Artist: " + artist, elementsColor, contentSize));
        }

        if (year != null) {
            consoleTable.addRowWithCells(new ConsoleTableCell("Year: " + year, elementsColor, contentSize));
        }

        if (duration != null) {
            consoleTable.addRowWithCells(new ConsoleTableCell("Duration: " + duration, elementsColor, contentSize));
        }

        if (genre != null) {
            consoleTable.addRowWithCells(new ConsoleTableCell("Genre: " + genre, elementsColor, contentSize));
        }

        consoleTable.addRowWithCells(new ConsoleTableCell("Has Cover: " + hasCover, elementsColor, contentSize));

        if (bitrate != null) {
            consoleTable.addRowWithCells(new ConsoleTableCell("Bitrate: " + bitrate, elementsColor, contentSize));
        }

        return consoleTable.draw();
    }

    public static String getLineInfo(Track track) {
        final SourceDataLine driver = track.getSpeaker().getDriver();

        return new StringBuilder().append("Soporte de controles en line")
                .append("---------------")
                .append("Pan: ").append(driver.isControlSupported(FloatControl.Type.PAN))
                .append("AuxReturn: ").append(driver.isControlSupported(FloatControl.Type.AUX_RETURN))
                .append("AuxSend: ").append(driver.isControlSupported(FloatControl.Type.AUX_SEND))
                .append("Balance: ").append(driver.isControlSupported(FloatControl.Type.BALANCE))
                .append("ReverbReturn: ").append(driver.isControlSupported(FloatControl.Type.REVERB_RETURN))
                .append("ReberbSend: ").append(driver.isControlSupported(FloatControl.Type.REVERB_SEND))
                .append("Volume: ").append(driver.isControlSupported(FloatControl.Type.VOLUME))
                .append("SampleRate: ").append(driver.isControlSupported(FloatControl.Type.SAMPLE_RATE))
                .append("MasterGain: ").append(driver.isControlSupported(FloatControl.Type.MASTER_GAIN))
                .toString();
    }

}
