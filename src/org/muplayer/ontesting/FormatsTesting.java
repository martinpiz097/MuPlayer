package org.muplayer.ontesting;

import org.muplayer.audio.Track;
import org.muplayer.audio.formats.FlacTrack;
import org.muplayer.audio.formats.M4ATrack;
import org.muplayer.audio.formats.OGGTrack;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class FormatsTesting {
    public static void main(String[] args) throws Exception {
        execOggTagTest();
    }

    public static void execFlacSeekTest()
            throws Exception {
        Track track = new FlacTrack("/" +
                "home/martin/AudioTesting/audio/flac.flac");
        new Thread(track).start();

        Scanner scan = new Scanner(System.in);
        String line;
        char first;

        while (true) {
            line = scan.nextLine();
            first = line.charAt(0);
            switch (first) {
                case 'k':
                    int seekSec = Integer.parseInt(line.substring(2));
                    track.seek(seekSec);
                    break;
            }

        }

    }

    public static void execM4ASeekTest() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        File sound = new File("/home/martin/AudioTesting/audio/m4a.m4a");
        Track track = new M4ATrack(sound);
        new Thread(track).start();

        Scanner scan = new Scanner(System.in);
        String line;
        char first;

        while (true) {
            line = scan.nextLine();

            first = line.charAt(0);
            switch (first) {
                case 'k':
                    int seekSec = Integer.parseInt(line.substring(2));
                    track.seek(seekSec);
                    break;
            }

        }
    }

    public static void execOggTagTest()
            throws UnsupportedAudioFileException,
            IOException, LineUnavailableException {
        File sound = new File("/home/martin/AudioTesting/audio/sound.ogg");
        Track track = new OGGTrack(sound);
        new Thread(track).start();
        track.setGain(0);
        System.out.println(track.getInfoSong());

    }

}
