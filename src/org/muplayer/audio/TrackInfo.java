package org.muplayer.audio;

import org.jaudiotagger.tag.FieldKey;

public interface TrackInfo {
    public boolean hasCover();
    String getProperty(String key);
    String getProperty(FieldKey key);
    public String getTitle();
    public String getAlbum();
    public String getArtist();
    public String getDate();
    public byte[] getCoverData();
    public long getDuration();

    public default String getDurationAsString() {
        return String.valueOf(getDuration());
    }

    public default String getFormattedDuration() {
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

    public String getEncoder();

}
