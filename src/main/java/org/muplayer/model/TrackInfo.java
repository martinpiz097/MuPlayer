package org.muplayer.model;

import org.jaudiotagger.tag.FieldKey;
import org.muplayer.system.Time;

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

    default String getFormattedDuration() {
        return Time.getInstance().getTimeFormatter().format(getDuration());
    }

    String getEncoder();
    String getBitrate();
}
