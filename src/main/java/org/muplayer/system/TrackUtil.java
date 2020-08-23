package org.muplayer.system;

import org.muplayer.audio.Track;

public class TrackUtil {
    private static void appendSongData(StringBuilder sbTabs, StringBuilder sbInfo, String title, String data) {

    }

    public static String getSongInfo(Track track) {
        StringBuilder sbInfo = new StringBuilder();
        String title = track.getTitle();
        String album = track.getAlbum();
        String artist = track.getArtist();
        String date = track.getDate();
        String duration = track.getDurationAsString();
        String hasCover = track.hasCover()?"Si":"No";
        String encoder = track.getEncoder();
        String bitrate = track.getBitrate();

        StringBuilder sbTabs = new StringBuilder();
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

        if (hasCover != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Has Cover: "+hasCover;
            if (biggerLenght < currentLine.length())
                biggerLenght = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }

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
}
