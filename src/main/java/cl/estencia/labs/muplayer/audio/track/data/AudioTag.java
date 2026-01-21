package cl.estencia.labs.muplayer.audio.track.data;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;

@Getter
@Setter
@Slf4j
public class AudioTag {
    private final AudioFile audioFile;

    public AudioTag(File trackFile) {
        this.audioFile = loadTags(trackFile);
    }

    private AudioFile loadTags(File trackFile) {
        try {
            return trackFile != null ? AudioFileIO.read(trackFile) : null;
        } catch (CannotReadException | InvalidAudioFrameException | ReadOnlyFileException | TagException | IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public boolean isValidFile() {
        return audioFile != null;
    }

    public Iterator<TagField> getTags() {
        return isValidFile() ? audioFile.getTag().getFields() : null;
    }

    public String getTag(FieldKey tag) {
        if (!isValidFile()) {
            return null;
        }

        final String tagValue = audioFile.getTag().getFirst(tag);
        return tagValue != null && !tagValue.isBlank() ? tagValue.trim() : null;
    }

    public String getTag(String tagName) {
        return getTag(FieldKey.valueOf(tagName.toUpperCase()));
    }

    public double getDuration() {
        return isValidFile() ? audioFile.getAudioHeader().getPreciseTrackLength() : 0;
    }

    public Artwork getCover() {
        return isValidFile() ? audioFile.getTag().getFirstArtwork() : null;
    }

    public byte[] getCoverData() {
        Artwork cover = getCover();
        return cover != null ? cover.getBinaryData() : null;
    }

}
