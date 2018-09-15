package org.muplayer.tests.ontesting;

import org.jflac.sound.spi.FlacAudioFileReader;
import org.jflac.sound.spi.FlacFormatConversionProvider;
import org.muplayer.audio.AudioTag;
import org.muplayer.audio.Track;
import org.muplayer.audio.formats.FlacTrack;
import org.muplayer.audio.formats.M4ATrack;
import org.muplayer.audio.formats.OGGTrack;
import org.muplayer.audio.formats.PCMTrack;
import org.muplayer.audio.interfaces.MusicControls;
import org.muplayer.audio.util.TrackHandler;
import org.muplayer.main.MusicPlayer;
import org.muplayer.system.Logger;
import org.muplayer.tests.TestingManager;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import static org.muplayer.tests.TestingKeys.TESTINGPATH;

public class FormatsTesting {

    private static TestingManager manager;

    static {
        try {
            manager = new TestingManager();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        //execM4ASeekTest();
        //execOggTagTest();
        executeFormatTest();
        //executeSongTesting(args[0]);
    }

    private static void executeSongTesting(String path) {
        try {
            new PCMTrack(path).start();
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            Logger.getLogger(FormatsTesting.class, "Exception", e.getMessage()).error();
        }
    }

    private static void executeFormatTest() {
        Scanner scan = new Scanner(System.in);
        String folderName;
        File folderTesting = null;
        MusicPlayer player = null;
            while (folderTesting == null || !folderTesting.exists()) {
                try {
                    System.out.println("Format to test: "+((folderName = scan.nextLine())));
                    folderTesting = new File(manager.getProperty(TESTINGPATH), folderName);
                    player = new MusicPlayer(folderTesting);
                } catch (FileNotFoundException e) {
                    folderTesting = null;
                }
            }
            player.start();
    }

    private static void execBytesTest() throws IOException, UnsupportedAudioFileException {
        String path = "/home/martin/AudioTesting/audio2/flac.flac";
        File audioFile = new File(path);
        FlacAudioFileReader audioReader = new FlacAudioFileReader();
        AudioInputStream flacAis = audioReader.getAudioInputStream(audioFile);

        AudioFormat format = flacAis.getFormat();
        AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(),
                format.getSampleSizeInBits(), format.getChannels(),
                format.getChannels() * 2, format.getSampleRate(),
                format.isBigEndian());

        AudioInputStream trackStream = new FlacFormatConversionProvider().
                getAudioInputStream(decodedFormat, flacAis);

        //byte[] bytes = new byte[(int) audioFile.length()];
        //System.out.println("FileLenght: "+audioFile.length());
        //System.out.println(decodedStream.read(new byte[20000]));
        //System.out.println("StreamAvailable: "+decodedStream.available());
        //System.out.println("ReadedData: "+decodedStream.read(bytes)/18432);


        long readed = 0;
        int seconds = 0;
        int count = 0;
        byte[] buffer = new byte[4096];
        System.out.println("FrameLenght: "+trackStream.getFrameLength());

        long ti = System.currentTimeMillis();
        while (trackStream.available() > 0) {
            readed+=(trackStream.read(buffer));
            if (System.currentTimeMillis() - ti >= 1000) {
                seconds++;
                System.out.println("Bytes por segundo: "+(readed/seconds));
                ti = System.currentTimeMillis();
            }
            count++;
        }
        System.out.println("Iterations: "+count);

    }

    public static void execTitleTest() throws Exception {
        String path = "/home/martin/AudioTesting/test/title.mp3";
        Track track = Track.getTrack(path);
        MusicControls controls = new TrackHandler(track);
        controls.play();
    }

    public static void execValidTest() throws Exception {
        String path = "/home/martin/AudioTesting/audio2/flac.flac";
        Track track = Track.getTrack(path);
        System.out.println(Track.isValidTrack(path));
        MusicControls controls = new TrackHandler(track);
        controls.play();

        String infoSong = track.getInfoSong();
        System.out.println(infoSong);
    }

    public static void execFlacSeekTest()
            throws Exception {
        Track track = new FlacTrack("/" +
                "home/martin/AudioTesting/audio2/flac.flac");
        new Thread(track).start();

        Scanner scan = new Scanner(System.in);
        String line;
        char first;
        AudioTag tagInfo = track.getTagInfo();
        System.out.println(tagInfo.getTagReader().toString());
        System.out.println(track.getDataSource().length());

        System.out.println("Duracion en segundos: "+track.getDurationAsString());
        System.out.println("Duracion formateada: "+track.getFormattedDuration());
        //track.finish();

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

    public static void execM4ASeekTest() throws Exception {
        File sound = new File("/home/martin/AudioTesting/audio2/m4a.m4a");
        TrackHandler handler = new TrackHandler(new M4ATrack(sound));
        handler.start();

        Scanner scan = new Scanner(System.in);
        String line;
        char first;

        while (true) {
            line = scan.nextLine();

            first = line.charAt(0);
            switch (first) {
                case 'k':
                    int seekSec = Integer.parseInt(line.substring(2));
                    handler.seek(seekSec);
                    break;
            }

        }
    }

    public static void execOggTagTest()
            throws UnsupportedAudioFileException,
            IOException, LineUnavailableException {
        File sound = new File("/home/martin/AudioTesting/audio2/au.ogg");
        Track track = new OGGTrack(sound);
        new Thread(track).start();
        //System.out.println(track.getInfoSong());

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
