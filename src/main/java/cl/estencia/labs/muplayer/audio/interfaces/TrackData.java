package cl.estencia.labs.muplayer.interfaces;

import org.jaudiotagger.tag.FieldKey;

public interface TrackData {
    boolean hasCover();
    String getProperty(String key);
    String getProperty(FieldKey key);
    String getTitle();
    String getAlbum();
    String getArtist();
    String getDate();
    byte[] getCoverData();

    String getEncoder();
    String getBitrate();
    String getFormat();
}
