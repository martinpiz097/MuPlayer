package cl.estencia.labs.muplayer.audio.interfaces;

import cl.estencia.labs.muplayer.audio.track.data.Cover;
import org.jaudiotagger.tag.FieldKey;

public interface TrackData {
    boolean hasCover();
    String getProperty(String key);
    String getProperty(FieldKey key);
    String getTitle();
    String getAlbum();
    String getArtist();
    String getYear();
    Cover getCover();
    byte[] getCoverData();

    String getEncoder();
    long getBitrate();
    String getFormat();
    String getGenre();
}
