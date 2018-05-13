package org.orangeplayer.audio;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;

import java.io.File;
import java.io.IOException;

public class AudioInfo {
    private AudioFile audioFile;
    private Tag fileTag;
    private AudioHeader header;

    public AudioInfo(File sound)
            throws TagException, ReadOnlyFileException,
            CannotReadException, InvalidAudioFrameException, IOException {
        this.audioFile = AudioFileIO.read(sound);
        fileTag = audioFile.getTag();
        header = audioFile.getAudioHeader();
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

}
