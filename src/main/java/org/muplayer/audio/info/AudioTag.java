package org.muplayer.audio.info;

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
import org.jaudiotagger.tag.datatype.Artwork;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class AudioTag {
    private File fileSource;
    private AudioFile audioFile;
    private Tag tagReader;
    private AudioHeader header;

    public AudioTag(File sound)
            throws TagException, ReadOnlyFileException,
            CannotReadException, InvalidAudioFrameException, IOException {
        this.fileSource = sound;
        this.audioFile = AudioFileIO.read(sound);
        tagReader = audioFile.getTag();
        header = audioFile.getAudioHeader();
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

    public File getFileSource() {
        return fileSource;
    }

    public Tag getTagReader() {
        return tagReader;
    }

    public AudioHeader getHeader() {
        return header;
    }

    public Iterator<TagField> getTags() {
        return tagReader.getFields();
    }

    public String getTag(FieldKey tag) {
        if (tagReader == null) {
            //System.out.println("Solicitando tag "+tag.name()+" nulo");
            return null;
        }
        String tagValue = tagReader.getFirst(tag);
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
        return tagReader.getFirstArtwork();
    }

    public byte[] getCoverData() {
        return hasCover() ? getCover().getBinaryData() : null;
    }

}
