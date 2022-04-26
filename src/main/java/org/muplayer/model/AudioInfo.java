/*package org.muplayer.audio.model;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.datatype.Artwork;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AudioInfo {
    private final AudioFile audioFile;
    private final Tag fileTag;
    private final AudioHeader header;

    public AudioInfo(File sound)
            throws TagException, ReadOnlyFileException,
            CannotReadException, InvalidAudioFrameException, IOException {
        this.audioFile = AudioFileIO.read(sound);
        fileTag = audioFile.getTag();
        header = audioFile.getAudioHeader();
    }

    public File getDataSource() {
        return audioFile.getFile();
    }

    public String getTag(FieldKey tag) {
        return fileTag.getFirst(tag);
    }

    public String getTag(String tagName) {
        return getTag(FieldKey.valueOf(tagName.toUpperCase()));
    }

    public int getDuration() {
        return header.getTrackLength();
    }

    public Artwork getCover() {
        return fileTag.getFirstArtwork();
    }

    public List<Artwork> getCovers() {
        return fileTag.getArtworkList();
    }

}
*/