package org.orangeplayer.audio.tracksFormats;

import org.orangeplayer.audio.Track;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class PCMTrack extends Track {

    public PCMTrack(File ftrack) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(ftrack);
    }

    public PCMTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath);
    }

    @Override
    protected void getAudioStream() throws IOException, UnsupportedAudioFileException {
        speakerAis = AudioSystem.getAudioInputStream(ftrack);
    }

    @Override
    public void seek(int seconds) {

    }

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        Track track = new PCMTrack("/home/martin/AudioTesting/audio/wav.wav");
        new Thread(track).start();
    }

}
