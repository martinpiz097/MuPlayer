package cl.estencia.labs.muplayer.core.util;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.io.TrackIOUtil;
import cl.estencia.labs.muplayer.console.enums.OutputType;
import cl.estencia.labs.muplayer.console.model.ConsoleTableLine;
import lombok.extern.slf4j.Slf4j;
import org.orangelogger.sys.ConsoleColor;
import org.orangelogger.sys.Logger;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ConsolePainterUtil {

    public static final String LINE_START = "│    ";
    public static final String LINE_END = "    │";
    public static final String TOP_LEFT_CORNER = "┌";
    public static final String TOP_RIGHT_CORNER = "┐";
    public static final String BOTTOM_LEFT_CORNER = "└";
    public static final String BOTTOM_RIGHT_CORNER = "┘";

    public static final String LINE_BREAK = "\n";
    public static final char DASH = '─';
    public static final char SPACE = ' ';
    public static final String ARROW = "→";
    public static final String LONG_ARROW = "⟶";
    public static final String DEFAULT_VALUES_SEPARATOR = ": ";

    public static void appendMargin(StringBuilder stringBuilder,
                                     int biggerLength, boolean top) {
        final int completeBiggerLength = biggerLength + LINE_START.length() + LINE_END.length();

        stringBuilder.append(top ? TOP_LEFT_CORNER : BOTTOM_LEFT_CORNER);
        for (int i = 0; i < completeBiggerLength - 2; i++) {
            stringBuilder.append(DASH);
        }
        stringBuilder.append(top ? TOP_RIGHT_CORNER : BOTTOM_RIGHT_CORNER);

        stringBuilder.append(LINE_BREAK);
    }

    public static String appendContentDetails(String str, int biggerLength) {
        final int strLength = str.length();
        final int lengthDiff = biggerLength - strLength;
        final StringBuilder sbSpaces = new StringBuilder();

        sbSpaces.append(LINE_START);
        sbSpaces.append(str);
        for (int i = 0; i < lengthDiff; i++) {
            sbSpaces.append(SPACE);
        }
        sbSpaces.append(LINE_END);

        return sbSpaces.toString();
    }

    public static void appendTableTitle(StringBuilder sbInfo, String tableTitle) {
        sbInfo.append(Logger.INFOCOLOR);

        if (tableTitle == null || tableTitle.isBlank()) {
            return;
        }

        sbInfo.append("  " + tableTitle.trim());
    }

    public static String createColoredString(Object data, OutputType outputType) {
        if (outputType == null) {
            return data.toString();
        }

        String coloredStr = switch (outputType) {
            case info -> Logger.INFOCOLOR + data.toString();
            case warn -> Logger.WARNINGCOLOR + data.toString();
            case error -> Logger.ERRORCOLOR + data.toString();
        };

        return coloredStr + ConsoleColor.RESET;
    }

    public static String createColoredStringLine(Object data, OutputType outputType) {
        return createColoredString(data, outputType) + LINE_BREAK;
    }

    public static String createConsoleTable(String tableTitle,
                                            List<ConsoleTableLine> tableLines) {
        final StringBuilder sbInfo = new StringBuilder();
        final List<String> listContentLines = CollectionUtil.newFastList(20);
        final AtomicInteger biggerLength = new AtomicInteger(0);

        appendTableTitle(sbInfo, tableTitle);

        tableLines.forEach(line -> {

            biggerLength.set(Math.max(line.getRawLineLength(), biggerLength.get()));
            listContentLines.add(line.getCompleteLine());
        });

        int contentLinesCount = listContentLines.size();
        for (int i = 0; i < contentLinesCount; i++) {
            listContentLines.set(i, appendContentDetails(
                    listContentLines.get(i), biggerLength.get()));
        }

        sbInfo.append(LINE_BREAK);
        appendMargin(sbInfo, biggerLength.get(), true);
        sbInfo.append(String.join(LINE_BREAK, listContentLines));
        sbInfo.append(LINE_BREAK);
        appendMargin(sbInfo, biggerLength.get(), false);

        sbInfo.deleteCharAt(sbInfo.length() - 1);
        return sbInfo.toString();
    }

    public static String createConsoleTable(List<ConsoleTableLine> tableLines) {
        return createConsoleTable(null, tableLines);
    }

    public static String getSongInfo(Track track) {
        final List<ConsoleTableLine> listTableLines = CollectionUtil.newFastList(10);

        final String title = track.getTitle();
        final String album = track.getAlbum();
        final String artist = track.getArtist();
        final String year = track.getYear();
        final String duration = track.getFormattedDuration();
        final String genre = track.getGenre();
        final String hasCover = track.hasCover() ? "Yes" : "No";
        final String bitrate = track.getBitrate();


        listTableLines.add(new ConsoleTableLine("Title", title, DEFAULT_VALUES_SEPARATOR));

        if (album != null) {
            listTableLines.add(new ConsoleTableLine("Album", album, DEFAULT_VALUES_SEPARATOR));
        }

        if (artist != null) {
            listTableLines.add(new ConsoleTableLine("Artist", artist, DEFAULT_VALUES_SEPARATOR));
        }

        if (year != null) {
            listTableLines.add(new ConsoleTableLine("Year", year, DEFAULT_VALUES_SEPARATOR));
        }

        if (duration != null) {
            listTableLines.add(new ConsoleTableLine("Duration", duration, DEFAULT_VALUES_SEPARATOR));
        }

        if (genre != null) {
            listTableLines.add(new ConsoleTableLine("Genre", genre, DEFAULT_VALUES_SEPARATOR));
        }

        listTableLines.add(new ConsoleTableLine("Has Cover", hasCover, DEFAULT_VALUES_SEPARATOR));

        if (bitrate != null) {
            listTableLines.add(new ConsoleTableLine("Bitrate", bitrate, DEFAULT_VALUES_SEPARATOR));
        }

        return createConsoleTable(listTableLines);
    }

    public static String getLineInfo(Track track) {
        final TrackIOUtil trackIOUtil = track.getTrackIOUtil();
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
