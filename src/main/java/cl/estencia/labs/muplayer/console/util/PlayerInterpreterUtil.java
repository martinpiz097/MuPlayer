package cl.estencia.labs.muplayer.console.util;

import cl.estencia.labs.aucom.core.util.ProcessManager;
import cl.estencia.labs.ebot.bus.MessageBus;
import cl.estencia.labs.ebot.bus.exception.BusException;
import cl.estencia.labs.muplayer.audio.common.enums.SeekOption;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.config.model.ConsoleCodesData;
import cl.estencia.labs.muplayer.config.reader.ConsoleCodesReader;
import cl.estencia.labs.muplayer.console.command.Command;
import cl.estencia.labs.muplayer.console.common.enums.ConsoleOutputMode;
import cl.estencia.labs.muplayer.console.model.ConsoleOutput;
import cl.estencia.labs.muplayer.console.model.table.*;
import cl.estencia.labs.muplayer.console.runner.ConsoleRunner;
import cl.estencia.labs.muplayer.console.runner.LocalRunner;
import cl.estencia.labs.muplayer.console.unix.NativeConsole;
import cl.estencia.labs.muplayer.core.bus.message.Messages;
import cl.estencia.labs.muplayer.core.bus.model.MuPlayerResponse;
import cl.estencia.labs.muplayer.core.bus.util.MessageBusUtil;
import cl.estencia.labs.muplayer.core.cache.CacheManager;
import cl.estencia.labs.muplayer.core.util.NumberUtil;
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

import static cl.estencia.labs.muplayer.audio.common.enums.SeekOption.NEXT;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.SPACE_CHAR;
import static cl.estencia.labs.muplayer.console.common.enums.ConsoleOutputMode.CLEAN;
import static cl.estencia.labs.muplayer.console.common.enums.ConsoleOutputMode.DEFAULT;
import static cl.estencia.labs.muplayer.console.common.enums.OutputType.*;
import static cl.estencia.labs.muplayer.console.util.ConsolePainter.printConsoleHeader;
import static cl.estencia.labs.muplayer.console.util.ConsoleUtil.getOutputColor;
import static cl.estencia.labs.muplayer.console.util.SystemCommandExecutor.clearConsole;
import static cl.estencia.labs.muplayer.console.util.SystemCommandExecutor.getClearConsoleOutput;
import static cl.estencia.labs.muplayer.core.cache.CacheVar.NATIVE_CONSOLE;
import static cl.estencia.labs.muplayer.core.cache.CacheVar.RUNNER;

@Slf4j
public class PlayerInterpreterUtil {

    private static final CacheManager GLOBAL_CACHE = CacheManager.getGlobalCache();

    public static void execSysCommand(String cmd) {
        try {
            String output = ProcessManager.executeLegacy(
                    cmd.split(String.valueOf(SPACE_CHAR)));
            ProcessManager.writeProcessOutputTo(output, SystemUtil.getStdout());
        } catch (IOException | InterruptedException e) {
            Logger.getLogger(PlayerInterpreterUtil.class, e.getMessage()).error();
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
        if (track == null) {
            return "";
        }

        String elementsColor = getOutputColor(info);
        ConsoleTable consoleTable = new ConsoleTable(null, elementsColor, Alignment.CENTER,
                new Padding(0, 3, 0, 3),
                false, false);

        final String title = track.getTitle();
        final String album = track.getAlbum();
        final String artist = track.getArtist();
        final String year = track.getYear();
        final String duration = track.getFormattedDuration();
        final String genre = track.getGenre();
        final String hasCover = track.hasCover() ? "Yes" : "No";
        final String bitrate = track.getBitrate() > -1 ? String.valueOf(track.getBitrate()) : "Unknown";

        int contentSize = 100;

        consoleTable.addRowWithCells(new ConsoleTableCell("Title: " + title, elementsColor, contentSize));

        if (album != null) {
            consoleTable.addRowWithCells(new ConsoleTableCell("Album: " + album, elementsColor, contentSize));
        }

        if (artist != null) {
            consoleTable.addRowWithCells(new ConsoleTableCell("Artist: " + artist, elementsColor, contentSize));
        }

        if (year != null) {
            consoleTable.addRowWithCells(new ConsoleTableCell(year.contains("-")
                    ? "Date: " : "Year: " + year, elementsColor, contentSize));
        }

        if (duration != null) {
            consoleTable.addRowWithCells(new ConsoleTableCell("Duration: " + duration, elementsColor, contentSize));
        }

        if (genre != null) {
            consoleTable.addRowWithCells(new ConsoleTableCell("Genre: " + genre, elementsColor, contentSize));
        }

        consoleTable.addRowWithCells(new ConsoleTableCell("Has Cover: " + hasCover, elementsColor, contentSize));

        consoleTable.addRowWithCells(new ConsoleTableCell("Bitrate: " + bitrate, elementsColor, contentSize));

        return consoleTable.draw();
    }

    public static String getTrackInfo(Track track, ConsoleOutputMode outputMode) {
        StringBuilder sbInfo = new StringBuilder();
        if (outputMode == CLEAN) {
            sbInfo.append(getClearConsoleOutput());
        }

        sbInfo.append(getTrackInfo(track));
        return sbInfo.toString();
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

    public static void printTrackInfo(Track track, ConsoleOutputMode outputMode) {
        String trackInfo = getTrackInfo(track, outputMode);
        IO.println(trackInfo);
    }

    public static void printConsoleLine() {
        ConsoleRunner consoleRunner = GLOBAL_CACHE.loadValue(RUNNER, ConsoleRunner.class);
        NativeConsole nativeConsole = GLOBAL_CACHE.loadValue(NATIVE_CONSOLE, NativeConsole.class);

        if (consoleRunner instanceof LocalRunner && (nativeConsole != null && nativeConsole.hasLine())) {
            IO.print(nativeConsole.getLine());
        }
    }

    public static void printConsoleInfo(Player player, ConsoleOutputMode outputMode, boolean withCurrentTrack) {
        if (withCurrentTrack) {
            printTrackInfo(player.getCurrentTrack().get(), outputMode);
        }

        printConsoleHeader(player, DEFAULT);
        printConsoleLine();
    }

    public static void printPlayerVolume(Player player, ConsoleOutput consoleOutput, boolean isSystemVolume) {
        if (!player.isAlive()) {
            return;
        }

        String volumeMsg = isSystemVolume
                ? "System Volume(0-100): " + player.getSystemVolume()
                : "Player Volume(0-100): " + player.getVolume();

        consoleOutput.append(volumeMsg, warn);
    }

    public static void changePlayerVolume(Player player, Command cmd, boolean isSystemVolume) {
        float volume;
        if (player.isAlive() && cmd.hasOptions()) {
            String firstOption = cmd.getOptionAt(0);
            Number volumeChange = NumberUtil.parseVolumeChange(firstOption);
            if (volumeChange != null) {
                volume = Math.max(
                        Math.min(player.getSystemVolume() + volumeChange.floatValue(), 100),
                        0);
            } else {
                Number volumeParam = NumberUtil.parseStringNumber(firstOption);
                if (volumeParam == null) {
                    return;
                }

                volume = volumeParam.floatValue();
            }

            if (volume == -1) {
                return;
            }

            if (isSystemVolume) {
                player.setSystemVolume(volume);
            } else {
                player.setVolume(volume);
            }
        }
    }

}
