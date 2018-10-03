package org.muplayer.audio.model;

import org.jaudiotagger.tag.FieldKey;

public interface TrackInfo {
    boolean hasCover();
    String getProperty(String key);
    String getProperty(FieldKey key);
    String getTitle();
    String getAlbum();
    String getArtist();
    String getDate();
    byte[] getCoverData();
    long getDuration();

    default String getDurationAsString() {
        return getFormattedDuration();
    }

    default String getFormattedDuration() {
        long duration = getDuration();
        if (duration < 60)
            return String.valueOf(duration);

        StringBuilder sbDuration = new StringBuilder();

        long minutes = duration / 60;
        long restSeconds = duration % 60;
        long hours = minutes / 60;
        long restMinutes = minutes % 60;

        if (hours > 0) {
            if (hours < 10)
                sbDuration.append('0');
            sbDuration.append(String.valueOf(hours));
            sbDuration.append(':');

            if (restMinutes < 10)
                sbDuration.append('0');
            sbDuration.append(String.valueOf(restMinutes));
            sbDuration.append(':');
        }

        else {
            if (minutes < 10)
                sbDuration.append('0');
            sbDuration.append(String.valueOf(minutes));
            sbDuration.append(':');
        }

        if (restSeconds < 10)
            sbDuration.append('0');
        sbDuration.append(String.valueOf(restSeconds));
        return sbDuration.toString();
    }

    String getEncoder();

}
