package org.muplayer.audio.model;

import org.jaudiotagger.tag.FieldKey;
import org.muplayer.audio.util.TimeFormatter;

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
        return TimeFormatter.format(getDuration());
    }

    String getEncoder();

}
