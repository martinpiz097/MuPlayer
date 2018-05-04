package org.orangeplayer.audio.tracksFormats;

import net.sourceforge.jaad.spi.javasound.AACAudioFileReader;
import org.orangeplayer.audio.Track;

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
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        audioReader = new AACAudioFileReader();
        speakerAis = audioReader.getAudioInputStream(ftrack);
        // Probar despues transformando a PCM
    }

    @Override
    public void seek(int seconds) throws Exception {
        speakerAis.skip(seconds);
    }

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        Track track = Track.getTrack("/home/martin/AudioTesting/audio/m4a3.m4a");
        new Thread(track).start();
    }

}
