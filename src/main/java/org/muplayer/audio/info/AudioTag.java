package org.muplayer.audio.info;

import lombok.Data;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.images.Artwork;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

@Data
public class AudioTag {
    private final File fileSource;
    private final AudioFile audioFile;
    private final Tag tagReader;
    private final AudioHeader header;

    public AudioTag(Object sound)
            throws TagException, ReadOnlyFileException,
            CannotReadException, InvalidAudioFrameException, IOException {
        if (sound instanceof File) {
            this.fileSource = (File) sound;
            this.audioFile = AudioFileIO.read(fileSource);
            tagReader = audioFile.getTag();
            header = audioFile.getAudioHeader();
        }
        else {
            this.fileSource = null;
            this.audioFile = null;
            tagReader = null;
            header = null;
        }
    }

    public AudioTag(String soundPath) throws
            ReadOnlyFileException, IOException, TagException,
            InvalidAudioFrameException, CannotReadException {
        this(new File(soundPath));
    }

    public boolean isValidFile() {
        return tagReader != null;
    }

    public boolean hasCover() {
        return isValidFile() && getCover() != null;
    }

    public Iterator<TagField> getTags() {
        return tagReader.getFields();
    }

    public String getTag(FieldKey tag) {
        if (tagReader == null)
            return null;
        final String tagValue = tagReader.getFirst(tag);
        return tagValue == null || tagValue.isEmpty() ?
                null : tagValue.trim();
    }

    public String getTag(String tagName) {
        return getTag(FieldKey.valueOf(tagName.toUpperCase()));
    }

    public int getDuration() {
        return header.getTrackLength();
    }

    public Artwork getCover() {
        return tagReader != null ? tagReader.getFirstArtwork() : null;
    }

    public byte[] getCoverData() {
        return hasCover() ? getCover().getBinaryData() : null;
    }

}
