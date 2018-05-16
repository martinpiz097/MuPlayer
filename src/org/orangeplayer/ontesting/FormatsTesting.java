package org.orangeplayer.ontesting;

import org.orangeplayer.audio.Track;
import org.orangeplayer.audio.formats.FlacTrack;
import sun.nio.ch.ThreadPool;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Scanner;

public class FormatsTesting {
    public static void main(String[] args) throws Exception {
        execFlacSeekTest();
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
}
