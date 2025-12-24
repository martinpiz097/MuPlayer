package cl.estencia.labs.muplayer.core.util;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.io.TrackIOUtil;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ConsolePainterUtil {

    private static final String LINE_START = "│    ";
    private static final String LINE_END = "    │";
    private static final String TOP_LEFT_CORNER = "┌";
    private static final String TOP_RIGHT_CORNER = "┐";
    private static final String BOTTOM_LEFT_CORNER = "└";
    private static final String BOTTOM_RIGHT_CORNER = "┘";

    private static final String LINE_BREAK = "\n";
    private static final char DASH = '─';
    public static final String DEFAULT_VALUES_SEPARATOR = ": ";

    private static void appendMargin(StringBuilder stringBuilder,
                                     int biggerLength, boolean top) {
        final int completeBiggerLength = biggerLength + LINE_START.length() + LINE_END.length();

        stringBuilder.append(top ? TOP_LEFT_CORNER : BOTTOM_LEFT_CORNER);
        for (int i = 0; i < completeBiggerLength - 2; i++) {
            stringBuilder.append(DASH);
        }
        stringBuilder.append(top ? TOP_RIGHT_CORNER : BOTTOM_RIGHT_CORNER);

        stringBuilder.append(LINE_BREAK);
    }

    private static String appendContentDetails(String str, int biggerLength) {
        final int strLength = str.length();
        final int lengthDiff = biggerLength - strLength;
        final StringBuilder sbSpaces = new StringBuilder();
        final char SPACE = ' ';

        sbSpaces.append(LINE_START);
        sbSpaces.append(str);
        for (int i = 0; i < lengthDiff; i++) {
            sbSpaces.append(SPACE);
        }
        sbSpaces.append(LINE_END);

        return sbSpaces.toString();
    }

    private static void appendTableTitle(StringBuilder sbInfo, String tableTitle) {
        if (tableTitle == null || tableTitle.isBlank()) {
            return;
        }

        sbInfo.append(tableTitle.trim());
        if (!tableTitle.endsWith(LINE_BREAK)) {
            sbInfo.append(LINE_BREAK);
        }
    }
    
    public static String getSongInfo(Track track) {
        final Map<String, Object> mapValues = new HashMap<>();

        final String title = track.getTitle();
        final String album = track.getAlbum();
        final String artist = track.getArtist();
        final String year = track.getYear();
        final String duration = track.getFormattedDuration();
        final String genre = track.getGenre();
        final String hasCover = track.hasCover() ? "Yes" : "No";
        final String bitrate = track.getBitrate();

        mapValues.put("Title", title);

        if (album != null) {
            mapValues.put("Album", album);
        }

        if (artist != null) {
            mapValues.put("Artist", artist);
        }

        if (year != null) {
            mapValues.put("Year", year);
        }

        if (duration != null) {
            mapValues.put("Duration", duration);
        }

        if (genre != null) {
            mapValues.put("Genre", genre);
        }

        mapValues.put("Has Cover", hasCover);
        if (bitrate != null) {
            mapValues.put("Bitrate", bitrate);
        }

        return getInfoTable(mapValues, DEFAULT_VALUES_SEPARATOR);
    }

    public static String getInfoTable(String tableTitle,
                                      Map<String, Object> mapInfo, String valuesSeparator) {
        final StringBuilder sbInfo = new StringBuilder();
        final List<String> listContentLines = CollectionUtil.newFastList(20);
        final AtomicInteger biggerLength = new AtomicInteger(0);

        appendTableTitle(sbInfo, tableTitle);
        mapInfo.forEach((title, value) -> {
            String currentLine = title + valuesSeparator + value.toString();
            biggerLength.set(Math.max(currentLine.length(), biggerLength.get()));

            listContentLines.add(currentLine);
        });

        int contentLinesCount = listContentLines.size();
        for (int i = 0; i < contentLinesCount; i++) {
            listContentLines.set(i, appendContentDetails(listContentLines.get(i), biggerLength.get()));
        }

        sbInfo.append(LINE_BREAK);
        appendMargin(sbInfo, biggerLength.get(), true);
        sbInfo.append(String.join(LINE_BREAK, listContentLines));
        sbInfo.append(LINE_BREAK);
        appendMargin(sbInfo, biggerLength.get(), false);

        sbInfo.deleteCharAt(sbInfo.length() - 1);
        return sbInfo.toString();
    }

    public static String getInfoTable(Map<String, Object> mapInfo, String valuesSeparator) {
        return getInfoTable(null, mapInfo, valuesSeparator);
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
