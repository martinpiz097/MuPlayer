package org.orangeplayer.audio.tracksFormats;

import org.orangeplayer.audio.Track;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class MP4Track extends Track {
    public MP4Track(File ftrack) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(ftrack);
    }

    public MP4Track(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath);
    }

    @Override
    protected void getAudioStream() throws IOException, UnsupportedAudioFileException {
        speakerAis = AudioSystem.getAudioInputStream(ftrack);
    }

    @Override
    public void seek(int seconds) throws Exception {

    }
}
