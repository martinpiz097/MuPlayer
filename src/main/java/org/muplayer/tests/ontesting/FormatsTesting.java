package org.muplayer.tests.ontesting;

import org.aucom.sound.AudioQuality;
import org.aucom.sound.Speaker;
import org.jflac.sound.spi.FlacAudioFileReader;
import org.jflac.sound.spi.FlacFormatConversionProvider;
import org.muplayer.audio.Player;
import org.muplayer.audio.Track;
import org.muplayer.audio.format.FlacTrack;
import org.muplayer.audio.format.M4ATrack;
import org.muplayer.audio.format.OGGTrack;
import org.muplayer.audio.format.PCMTrack;
import org.muplayer.audio.info.AudioTag;
import org.muplayer.audio.interfaces.MusicControls;
import org.muplayer.audio.model.Album;
import org.muplayer.audio.model.Artist;
import org.muplayer.audio.model.TrackInfo;
import org.muplayer.audio.util.TrackHandler;
import org.muplayer.main.ConsoleOrder;
import org.muplayer.main.ConsolePlayer;
import org.muplayer.util.TrackUtil;
import org.muplayer.tests.TestingManager;
import org.muplayer.thread.TaskRunner;
import org.orangelogger.sys.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Scanner;
import java.util.Set;

import static org.muplayer.tests.TestingKeys.TESTINGPATH;

public class FormatsTesting {

    private static TestingManager manager;
    private static File TEST_FOLDER;

    static {
        try {
            TEST_FOLDER = new File("audio");
            if (TEST_FOLDER.exists())
                TEST_FOLDER = TEST_FOLDER.getCanonicalFile();
            else
                TEST_FOLDER = new File("muplayer/audio").getCanonicalFile();
            manager = new TestingManager();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        execTest();
        //execConsolePlayerTest(new File("/home/martin/Escritorio/Música"));
        //execArtistsTest();
        //execAlbumsTest();
        //execSpeakerTest();
    }

    private static void execSpeakerTest() throws LineUnavailableException {
        final Speaker speaker = new Speaker(AudioQuality.NORMAL);
        speaker.open();
        System.out.println(speaker.getDriver());
    }

    private static void execArtistsTest() throws FileNotFoundException {
        Player player = new Player(new File("/home/martin/Escritorio/Música"));
        player.start();
        player.mute();
        Collection<Artist> artists = player.getArtists();
        artists.forEach(artist -> {
            System.out.println("Artista: " + artist.getName());
            Set<TrackInfo> tracksSet = artist.getTracksSet();
            System.out.println("\t".concat("Songs: "+ tracksSet.size()));
            for(TrackInfo track : tracksSet) {
                System.out.println("\t".concat("Song: "+track.getTitle()));
            }
        });
    }

    private static void execAlbumsTest() throws FileNotFoundException {
        Player player = new Player(new File("/home/martin/Escritorio/Música"));
        player.start();
        player.mute();
        Collection<Album> artists = player.getAlbums();
        artists.forEach(album -> {
            System.out.println("Album: " + album.getName());
            Set<TrackInfo> tracksSet = album.getTracksSet();
            System.out.println("\t".concat("Songs: "+ tracksSet.size()));
            for(TrackInfo track : tracksSet) {
                System.out.println("\t".concat("Song: "+track.getTitle()));
            }
        });
    }

    private static void execConsolePlayerTest(File folder) throws FileNotFoundException {
        TaskRunner.execute(new ConsolePlayer(folder));
    }

    private static void execTest() throws FileNotFoundException {
        Scanner scan = new Scanner(System.in);
        System.out.print("Format to Scan: ");
        String format;

        do {
            format = scan.nextLine();
        } while (format == null || format.isEmpty());

        execTest(format);
    }

    private static void execTest(String format) throws FileNotFoundException {
        try {
            System.out.println("TestFolderPath: "+TEST_FOLDER.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        File formatFolder = new File(TEST_FOLDER, format);
        ConsolePlayer player = new ConsolePlayer(formatFolder);
        TaskRunner.execute(player);
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
        ConsolePlayer player = null;
        while (folderTesting == null || !folderTesting.exists()) {
            try {
                System.out.println("Format to test: "+((folderName = scan.nextLine())));
                if (folderName.equals("music"))
                    folderTesting = new File("/home/martin/Escritorio/Archivos/Música");
                else
                    folderTesting = new File(manager.getProperty(TESTINGPATH), folderName);
                player = new ConsolePlayer(folderTesting);
            } catch (FileNotFoundException e) {
                folderTesting = null;
            }
        }
        TaskRunner.execute(player);
        player.execCommand(ConsoleOrder.START);
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

        String infoSong = TrackUtil.getSongInfo(track);
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
        System.out.println(((File)track.getDataSource()).length());

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
        //System.out.println(track.getSongInfo());

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
