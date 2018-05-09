package org.orangeplayer.audio.formats;

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
        //String strTrack = "/home/martin/AudioTesting/music/John Petrucci/" +
          //      "When_The_Keyboard_Breaks_Live_In_Chicago/Universal_Mind.m4a";
        //Track track = Track.getTrack(strTrack);
        //Track track = new PCMTrack(strTrack);
        new Thread(track).start();
    }

}
