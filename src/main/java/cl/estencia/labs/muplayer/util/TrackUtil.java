package cl.estencia.labs.muplayer.util;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.TrackIO;
import lombok.extern.java.Log;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Log
public class TrackUtil {

    public String getSongInfo(Track track) {
        final StringBuilder sbInfo = new StringBuilder();
        final String title = track.getTitle();
        final String album = track.getAlbum();
        final String artist = track.getArtist();
        final String date = track.getDate();
        final String duration = track.getFormattedDuration();
        final String hasCover = track.hasCover() ? "Yes" : "No";
        final String encoder = track.getEncoder();
        final String bitrate = track.getBitrate();

        final StringBuilder sbTabs = new StringBuilder();
        String currentLine = "Song: " + title;
        int biggerLength = currentLine.length();
        sbInfo.append(currentLine).append('\n');

        if (album != null) {
            sbTabs.append("    ");
            currentLine = sbTabs + "Album: " + album;
            if (biggerLength < currentLine.length()) {
                biggerLength = currentLine.length();
            }
            sbInfo.append(currentLine).append('\n');
        }

        if (artist != null) {
            sbTabs.append("    ");
            currentLine = sbTabs + "Artist: " + artist;
            if (biggerLength < currentLine.length()) {
                biggerLength = currentLine.length();
            }
            sbInfo.append(currentLine).append('\n');
        }

        if (date != null) {
            sbTabs.append("    ");
            currentLine = sbTabs + "Date: " + date;
            if (biggerLength < currentLine.length()) {
                biggerLength = currentLine.length();
            }
            sbInfo.append(currentLine).append('\n');
        }

        if (duration != null) {
            sbTabs.append("    ");
            currentLine = sbTabs + "Duration: " + duration;
            if (biggerLength < currentLine.length()) {
                biggerLength = currentLine.length();
            }
            sbInfo.append(currentLine).append('\n');
        }

        sbTabs.append("    ");
        currentLine = sbTabs + "Has Cover: " + hasCover;
        if (biggerLength < currentLine.length()) {
            biggerLength = currentLine.length();
        }
        sbInfo.append(currentLine).append('\n');

        if (encoder != null) {
            sbTabs.append("    ");
            currentLine = sbTabs + "Encoder: " + encoder;
            if (biggerLength < currentLine.length()) {
                biggerLength = currentLine.length();
            }
            sbInfo.append(currentLine).append('\n');
        }

        if (bitrate != null) {
            sbTabs.append("    ");
            currentLine = sbTabs + "Bitrate: " + bitrate + " kbps";
            if (biggerLength < currentLine.length()) {
                biggerLength = currentLine.length();
            }
            sbInfo.append(currentLine).append('\n');
        }

        sbTabs.delete(0, sbTabs.length());

        for (int i = 0; i < biggerLength; i++) {
            sbTabs.append('-');
        }
        sbTabs.append('\n').append(sbInfo);

        for (int i = 0; i < biggerLength; i++) {
            sbTabs.append('-');
        }
        sbTabs.append('\n');

        return sbTabs.toString();
    }

    public String getLineInfo(Track track) {
        final TrackIO trackIO = track.getTrackIO();
        final SourceDataLine driver = trackIO.getSpeaker().getDriver();

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
