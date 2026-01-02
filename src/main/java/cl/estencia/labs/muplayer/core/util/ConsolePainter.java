package cl.estencia.labs.muplayer.core.util;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.io.TrackIOUtil;
import cl.estencia.labs.muplayer.console.common.enums.OutputType;
import cl.estencia.labs.muplayer.console.model.ConsoleTableLine;
import lombok.extern.slf4j.Slf4j;
import org.orangelogger.sys.ConsoleColor;
import org.orangelogger.sys.Logger;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static cl.estencia.labs.muplayer.console.common.ConsoleSymbols.*;

@Slf4j
public class ConsolePainter {

    public static void appendMargin(StringBuilder stringBuilder,
                                     int biggerLength, boolean top) {
        final int completeBiggerLength = biggerLength + DOUBLE_LINE_START.length() + DOUBLE_LINE_END.length();

        stringBuilder.append(top ? DOUBLE_TOP_LEFT_CORNER : DOUBLE_BOTTOM_LEFT_CORNER);
        for (int i = 0; i < completeBiggerLength - 2; i++) {
            stringBuilder.append(DOUBLE_HORIZONTAL_LINE);
        }
        stringBuilder.append(top ? DOUBLE_TOP_RIGHT_CORNER : DOUBLE_BOTTOM_RIGHT_CORNER);

        stringBuilder.append(LINE_BREAK);
    }

    public static String appendContentDetails(String str, int biggerLength) {
        final int strLength = str.length();
        final int lengthDiff = biggerLength - strLength;
        final StringBuilder sbSpaces = new StringBuilder();

        sbSpaces.append(DOUBLE_LINE_START);
        sbSpaces.append(str);
        for (int i = 0; i < lengthDiff; i++) {
            sbSpaces.append(SPACE);
        }
        sbSpaces.append(DOUBLE_LINE_END);

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

    public static String paintVolumeIconByPercent(Number volume) {
        int volumeInt = volume.intValue();

        if (volumeInt >= 80) {
            return HIGH_VOLUME;
        }
        if (volumeInt >= 40) {
            return MED_VOLUME;
        }
        else if (volumeInt > 0) {
            return LOW_VOLUME;
        }
        else {
            return MUTED;
        }
    }

    public static String paintVolumePercent(Number volume) {
        return volume.intValue() + "%";
    }

    public static String paintVolumeBar(Number volume) {
        final StringBuilder sbVolume = new StringBuilder();

//        █████░░░ 60%

        // por si por error tengo volumenes mayores a 100
        final int adjustedVol = Math.min(volume.intValue(), 100);
        final int scale = 20;
        final int scaledVol = Math.toIntExact(Math.round(((float) (scale * adjustedVol)) / 100));

        for (int i = 0; i < scaledVol; i++) {
            sbVolume.append(COMPLETE_BLOCK);
        }

        for (int i = scaledVol; i < scale; i++) {
            sbVolume.append(EMPTY_BLOCK);
        }

        return sbVolume.toString();
    }

    public static String paintMusicPlayerIcons(boolean isPlaying) {
        return new StringBuilder()
                .append('(')
                .append(PREV).append(SPACE)
                .append(isPlaying ? PAUSE : PLAY)
                .append(SPACE).append(NEXT)
                .append(')')
                .toString();
    }

}
