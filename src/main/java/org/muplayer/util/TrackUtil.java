package org.muplayer.util;

import org.muplayer.audio.track.Track;
import org.muplayer.audio.track.TrackIO;
import org.muplayer.audio.player.Player;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;

public class TrackUtil {
    public static String getSongInfo(Track track) {
        final StringBuilder sbInfo = new StringBuilder();
        final String title = track.getTitle();
        final String album = track.getAlbum();
        final String artist = track.getArtist();
        final String date = track.getDate();
        final String duration = track.getFormattedDuration();
        final String hasCover = track.hasCover()?"Yes":"No";
        final String encoder = track.getEncoder();
        final String bitrate = track.getBitrate();

        final StringBuilder sbTabs = new StringBuilder();
        String currentLine = "Song: "+title;
        int biggerLength = currentLine.length();
        sbInfo.append(currentLine).append('\n');

        if (album != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Album: "+album;
            if (biggerLength < currentLine.length())
                biggerLength = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }

        if (artist != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Artist: "+artist;
            if (biggerLength < currentLine.length())
                biggerLength = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }

        if (date != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Date: "+date;
            if (biggerLength < currentLine.length())
                biggerLength = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }

        if (duration != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Duration: "+duration;
            if (biggerLength < currentLine.length())
                biggerLength = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }

        sbTabs.append("    ");
        currentLine = sbTabs.toString()+"Has Cover: "+hasCover;
        if (biggerLength < currentLine.length())
            biggerLength = currentLine.length();
        sbInfo.append(currentLine).append('\n');

        if (encoder != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Encoder: "+encoder;
            if (biggerLength < currentLine.length())
                biggerLength = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }

        if (bitrate != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Bitrate: "+bitrate+" kbps";
            if (biggerLength < currentLine.length())
                biggerLength = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }

        sbTabs.delete(0, sbTabs.length());

        for (int i = 0; i < biggerLength; i++)
            sbTabs.append('-');
        sbTabs.append('\n').append(sbInfo.toString());

        for (int i = 0; i < biggerLength; i++)
            sbTabs.append('-');
        sbTabs.append('\n');

        return sbTabs.toString();
    }

    public static Constructor<? extends Track> getTrackClassConstructor(String formatClass, Class<?>... paramsClasses) {
        try {
            final Class<? extends Track> trackClass = (Class<? extends Track>)
                    Class.forName(formatClass);
            return trackClass.getConstructor(paramsClasses);
        } catch (Exception e) {
            return null;
        }
    }

    public static Track getTrackFromClass(String formatClass, File dataSource, Player player) {
        try {
            final Constructor<? extends Track> constructor = getTrackClassConstructor(
                    formatClass, dataSource.getClass(), Player.class);
            return constructor != null ? constructor.newInstance(dataSource, player) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getLineInfo(Track track) {
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
