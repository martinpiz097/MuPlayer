package org.muplayer.tests.other;

import org.muplayer.audio.format.OGGTrack;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class CompatiblityTest {
    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        File sound = new File("/home/martin/AudioTesting/music/newogg.ogg");
        OGGTrack track = new OGGTrack(sound);
        track.setGain(80);
        track.run();
    }
}
