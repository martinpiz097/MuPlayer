package org.muplayer.ontesting;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;

public class AudioTaggerTest {
    public static void main(String[] args) throws TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException, IOException {
        AudioFile file = AudioFileIO.read(
                new File("/home/martin/AudioTesting/audio/au.mp3"));
        File fCover = new File(file.getFile().getParent(), "cover.jpg");
        fCover.createNewFile();
        int seconds = file.getAudioHeader().getTrackLength();
        System.out.println(seconds);
        Tag tag = file.getTag();
        //System.out.println("Tag: "+tag);
        //System.out.println(tag.getFirst(FieldKey.COVER_ART));
        //Artwork cover = tag.getFirstArtwork();
        //Files.write(fCover.toPath(), cover.getBinaryData(), StandardOpenOption.TRUNCATE_EXISTING);
        /*List<Artwork> listArtwork = tag.getArtworkList();

        for (int i = 0; i < listArtwork.size(); i++) {
            System.out.println(i+""+listArtwork.get(i).getDescription());
        }*/

    }
}
