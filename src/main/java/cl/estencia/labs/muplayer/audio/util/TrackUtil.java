package cl.estencia.labs.muplayer.audio.util;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.io.TrackIOUtil;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.tag.FieldKey;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TrackUtil {

    private static void appendMargin(StringBuilder stringBuilder, int biggerLength) {
        for (int i = 0; i < biggerLength; i++) {
            stringBuilder.append('-');
        }

        stringBuilder.append('\n');
    }

    public static String getSongInfo(Track track) {
        final StringBuilder sbInfo = new StringBuilder();
        final StringBuilder sbContent = new StringBuilder();

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
        sbContent.append(currentLine).append('\n');

        if (album != null) {
            currentLine = "Album: " + album;
            biggerLength = Math.max(biggerLength, currentLine.length());
            sbContent.append(currentLine).append('\n');
        }

        if (artist != null) {
            currentLine = "Artist: " + artist;
            biggerLength = Math.max(biggerLength, currentLine.length());
            sbContent.append(currentLine).append('\n');
        }

        if (year != null) {
            currentLine = "Year: " + year;
            biggerLength = Math.max(biggerLength, currentLine.length());
            sbContent.append(currentLine).append('\n');
        }

        if (duration != null) {
            currentLine = "Duration: " + duration;
            biggerLength = Math.max(biggerLength, currentLine.length());
            sbContent.append(currentLine).append('\n');
        }

        if (genre != null) {
            currentLine = "Genre: " + genre;
            biggerLength = Math.max(biggerLength, currentLine.length());
            sbContent.append(currentLine).append('\n');
        }

        currentLine = "Has Cover: " + hasCover;
        biggerLength = Math.max(biggerLength, currentLine.length());
        sbContent.append(currentLine).append('\n');

        if (bitrate != null) {
            currentLine = "Bitrate: " + bitrate + " kbps";
            biggerLength = Math.max(biggerLength, currentLine.length());
            sbContent.append(currentLine).append('\n');
        }

        appendMargin(sbInfo, biggerLength);
        sbInfo.append(sbContent);
        appendMargin(sbInfo, biggerLength);

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
