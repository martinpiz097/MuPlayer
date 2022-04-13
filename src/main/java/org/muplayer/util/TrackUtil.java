package org.muplayer.util;

import org.muplayer.audio.Track;
import org.muplayer.audio.interfaces.PlayerControls;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;

public class TrackUtil {
    private static void appendSongData(StringBuilder sbTabs, StringBuilder sbInfo, String title, String data) {

    }

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
        int biggerLenght = currentLine.length();
        sbInfo.append(currentLine).append('\n');

        if (album != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Album: "+album;
            if (biggerLenght < currentLine.length())
                biggerLenght = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }

        if (artist != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Artist: "+artist;
            if (biggerLenght < currentLine.length())
                biggerLenght = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }

        if (date != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Date: "+date;
            if (biggerLenght < currentLine.length())
                biggerLenght = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }

        if (duration != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Duration: "+duration;
            if (biggerLenght < currentLine.length())
                biggerLenght = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }

        sbTabs.append("    ");
        currentLine = sbTabs.toString()+"Has Cover: "+hasCover;
        if (biggerLenght < currentLine.length())
            biggerLenght = currentLine.length();
        sbInfo.append(currentLine).append('\n');

        if (encoder != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Encoder: "+encoder;
            if (biggerLenght < currentLine.length())
                biggerLenght = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }

        if (bitrate != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Bitrate: "+bitrate+" kbps";
            if (biggerLenght < currentLine.length())
                biggerLenght = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }

        sbTabs.delete(0, sbTabs.length());

        for (int i = 0; i < biggerLenght; i++)
            sbTabs.append('-');
        sbTabs.append('\n').append(sbInfo.toString());

        for (int i = 0; i < biggerLenght; i++)
            sbTabs.append('-');
        sbTabs.append('\n');

        return sbTabs.toString();
    }

    public static Constructor<? extends Track> getTrackClassConstructor(String formatClass, Class<?>... paramsClasses) {
        final Class<? extends Track> trackClass;
        try {
            trackClass = (Class<? extends Track>) Class.forName(formatClass);
            return trackClass.getConstructor(paramsClasses);
        } catch (Exception e) {
            return null;
        }
    }

    public static Track getTrackFromClass(String formatClass, File dataSource, PlayerControls player) {
        try {
            final Constructor<? extends Track> constructor
                    = getTrackClassConstructor(formatClass, dataSource.getClass(), PlayerControls.class);
            return constructor != null ? constructor.newInstance(dataSource, player) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Track getTrackFromClass(String formatClass, InputStream dataSource, PlayerControls player) {
        try {
            final Constructor<? extends Track> constructor
                    = getTrackClassConstructor(formatClass, dataSource.getClass(), PlayerControls.class);
            return constructor != null ? constructor.newInstance(dataSource, player) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
