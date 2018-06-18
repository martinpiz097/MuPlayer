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
}
