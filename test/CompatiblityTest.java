package org.orangeplayer.test;

import org.orangeplayer.audio.Track;

import java.io.File;

public class CompatiblityTest {
    public static void main(String[] args) {
        File sound = new File("/home/martin/AudioTesting/music/Alejandro Silva/1 - 1999/AlbumArtSmall.jpg");
        Track.getTrack(sound);
    }
}
