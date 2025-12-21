package cl.estencia.labs.muplayer.audio.util;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.io.TrackIOUtil;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.util.List;

@Slf4j
public class TrackUtil {

    private static final String LINE_START = "│    ";
    private static final String LINE_END = "    │";
    private static final String TOP_LEFT_CORNER = "┌";
    private static final String TOP_RIGHT_CORNER = "┐";
    private static final String BOTTOM_LEFT_CORNER = "└";
    private static final String BOTTOM_RIGHT_CORNER = "┘";

    private static final String LINE_BREAK = "\n";
    private static final char DASH = '─';

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
    
    private static int completeLineLength(String originalLine) {
        return originalLine.length() + LINE_START.length() + LINE_END.length();
    }

    public static String getSongInfo(Track track) {
        final StringBuilder sbInfo = new StringBuilder();
        final List<String> listContentLines = CollectionUtil.newFastList(20);

        final String title = track.getTitle();
        final String album = track.getAlbum();
        final String artist = track.getArtist();
        final String year = track.getYear();
        final String duration = track.getFormattedDuration();
        final String genre = track.getGenre();
        final String hasCover = track.hasCover() ? "Yes" : "No";
        final String bitrate = track.getBitrate();

        String currentLine = "Title: " + title;
        int biggerLength = currentLine.length();
        listContentLines.add(currentLine);

        if (album != null) {
            currentLine = "Album: " + album;
            biggerLength = Math.max(biggerLength, currentLine.length());
            listContentLines.add(currentLine);
        }

        if (artist != null) {
            currentLine = "Artist: " + artist;
            biggerLength = Math.max(biggerLength, currentLine.length());
            listContentLines.add(currentLine);
        }

        if (year != null) {
            currentLine = "Year: " + year;
            biggerLength = Math.max(biggerLength, currentLine.length());
            listContentLines.add(currentLine);
        }

        if (duration != null) {
            currentLine = "Duration: " + duration;
            biggerLength = Math.max(biggerLength, currentLine.length());
            listContentLines.add(currentLine);
        }

        if (genre != null) {
            currentLine = "Genre: " + genre;
            biggerLength = Math.max(biggerLength, currentLine.length());
            listContentLines.add(currentLine);
        }

        currentLine = "Has Cover: " + hasCover;
        biggerLength = Math.max(biggerLength, currentLine.length());
        listContentLines.add(currentLine);

        if (bitrate != null) {
            currentLine = "Bitrate: " + bitrate + " kbps";
            biggerLength = Math.max(biggerLength, currentLine.length());
            listContentLines.add(currentLine);
        }

        int contentLinesCount = listContentLines.size();
        for (int i = 0; i < contentLinesCount; i++) {
            listContentLines.set(i, appendContentDetails(listContentLines.get(i), biggerLength));
        }

        sbInfo.append(LINE_BREAK);
        appendMargin(sbInfo, biggerLength, true);
        sbInfo.append(String.join(LINE_BREAK, listContentLines));
        sbInfo.append(LINE_BREAK);
        appendMargin(sbInfo, biggerLength, false);

        sbInfo.deleteCharAt(sbInfo.length() - 1);
        return sbInfo.toString();
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
