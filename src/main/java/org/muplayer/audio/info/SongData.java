package org.muplayer.audio.info;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.datatype.Artwork;
import org.muplayer.audio.model.TrackInfo;

public class SongData implements TrackInfo {

    private final AudioTag info;

    public SongData(AudioTag tag) {
        this.info = tag;
    }



    @Override
    public boolean hasCover() {
        return info.hasCover();
    }

    @Override
    public String getProperty(String key) {
        return info.getTag(key);
    }

    @Override
    public String getProperty(FieldKey key) {
        return info.getTag(key);
    }

    @Override
    public String getTitle() {
        String title = info.getTag(FieldKey.TITLE);
        if (title == null)
            title = info.getFileSource().getName();
        return title;
    }

    @Override
    public String getAlbum() {
        String album = info.getTag(FieldKey.ALBUM);
        if (album == null)
            album = "Album Desconocido";
        return album;
    }

    @Override
    public String getArtist() {
        String artist = info.getTag(FieldKey.ARTIST);
        if (artist == null)
            artist = "Artista Desconocido";
        return artist;
    }

    @Override
    public String getDate() {
        return info.getTag(FieldKey.YEAR);
    }

    @Override
    public byte[] getCoverData() {
        Artwork cover = info.getCover();
        return cover == null ? null : cover.getBinaryData();
    }

    @Override
    public long getDuration() {
        return info.getDuration();
    }

    @Override
    public String getEncoder() {
        return info.getTag(FieldKey.ENCODER);
    }

    @Override
    public String getBitrate() {
        return info.getHeader().getBitRate();
    }

    @Override
    public String toString() {
        return "SongData: "+getTitle();
    }
}
