package org.muplayer.main;

import org.muplayer.audio.model.SeekOption;
import org.muplayer.audio.Track;
import org.orangelogger.sys.Logger;

import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class MusicPlayer extends ConsolePlayer {

    public MusicPlayer(File rootFolder) throws FileNotFoundException {
        super(rootFolder);
    }

    public MusicPlayer(String folder) throws FileNotFoundException {
        super(folder);
    }

    @Override
    public void run() {
        System.out.println("Sounds total: "+player.getSongsCount());
        player.start();

        Scanner scan = new Scanner(System.in);
        SourceDataLine playerLine = null;

        char c;
        String line;

        boolean on = true;

        while (on) {
            try {
                line = scan.nextLine().trim();
                c = line.charAt(0);
                switch (c) {
                    case 'n':
                        if (line.length() > 1)
                            if (line.length() == 2 && line.charAt(1) == 'f')
                                player.seekFolder(SeekOption.NEXT);
                            else
                                player.jumpTrack(Integer.parseInt(line.substring(2)), SeekOption.NEXT);
                        else
                            player.playNext();
                        break;

                    case 'p':
                        if (line.length() > 1)
                            if (line.length() == 2 && line.charAt(1) == 'f')
                                player.seekFolder(SeekOption.PREV);
                            else
                                player.jumpTrack((Integer.parseInt(line.substring(2))), SeekOption.PREV);
                        else
                            player.playPrevious();
                        break;
                    case 's':
                        player.stopTrack();
                        break;
                    case 'r':
                        player.resumeTrack();
                        break;
                    case 'm':
                        player.pause();
                        break;
                    case 'v':
                        player.setGain(Float.parseFloat(line.substring(2).trim()));
                        break;
                    case 'k':
                        player.seek(Integer.parseInt(line.substring(2).trim()));
                        break;
                    case 'e':
                        on = false;
                        player.shutdown();
                        break;
                    case 'u':
                        player.reloadTracks();
                        break;
                    case 'w':
                        System.out.println(player.getProgress());
                        break;
                    case 'g':
                        player.getCurrent().gotoSecond(
                                Integer.parseInt(line.substring(2).trim()));
                        break;
                    case 'c':
                        System.out.println(player.getSongsCount());
                        break;
                    case 'l':
                        int lineLen = line.length();
                        if (lineLen == 2) {
                            if (line.charAt(1) == 'f')
                                player.printFolders();
                            else if (line.charAt(1) == 'c')
                                player.printFolderTracks();
                        }
                        else
                            player.printTracks();
                        break;

                    case 'd':
                        System.out.println(player.getCurrent().getDurationAsString());
                        break;

                    case 'x':
                        System.out.println("ParentFile: "+player.getCurrent().getDataSource().getParentFile().getName());
                        break;
                    case 'f':
                        Track cur = player.getCurrent();
                        String curClassName = cur.getClass().getSimpleName();
                        curClassName = curClassName.substring(0, curClassName.length()-5).toLowerCase();
                        System.out.println(cur.getFormat()+' '+curClassName);
                        break;
                    case 'i':
                        player.getCurrent().getLineInfo();
                        break;
                    case '1':
                        System.out.println(player.getCurrent()
                                .getTrackLine().getDriver().getMicrosecondPosition()/1000000);
                        break;
                    case '2':
                        SourceDataLine driver = player.getCurrent().getTrackLine().getDriver();
                        double seconds = ((double)driver.getMicrosecondPosition())/1000000;
                        long longFramePosition = driver.getLongFramePosition();
                        float frameRate = driver.getFormat().getFrameRate();
                        int frameSize = driver.getFormat().getFrameSize();
                        // framepos es igual a currentFrames*frameSize;
                        Track current = player.getCurrent();
                        double currentFrames = frameRate*seconds;
                        double totalFrames = frameRate*current.getDuration();
                        System.out.println("TotalFrames: "+totalFrames);
                        System.out.println("TotalSize: "+Math.round(totalFrames*frameSize));
                        System.out.println("FileLenght: "+current.getDataSource().length());
                        //System.out.println("BytesCount: "+player.getCurrent().getBytesPerSecond()*seconds);
                        break;
                    case '3':
                        System.out.println(player.getCurrent()
                                .getTrackLine().getDriver().available());
                        break;
                    /*case 'o':
                        //AudioFileFormat format = DigitalAudioSystem.getAudioFileFormat(player.getCurrent().getDataSource());
                        AudioFormat format = player.getCurrent().getDecodedStream().getFormat();
                        Iterator<Map.Entry<String, Object>> it = format.properties().entrySet().iterator();
                        System.out.println("AudioFileFormat Properties!");
                        System.out.println("---------------------------");
                        Map.Entry<String, Object> next;
                        //ByteArrayInputStream bais = null;
                        while (it.hasNext()) {
                            next = it.next();
                            System.out.println(next.getKey()+'='+next.getValue().toString());
                            /*if (next.getKey().equals("mp3.id3tag.v2")) {
                                System.out.println("Bais: "+next.getValue());
                                bais = (ByteArrayInputStream) next.getValue();
                            }*/
                        }
                        /*File cover = new File("/home/martin/AudioTesting/data");
                        cover.createNewFile();
                        byte[] bytes = new byte[bais.available()];
                        bais.read(bytes);
                        Files.write(cover.toPath(), bytes, StandardOpenOption.TRUNCATE_EXISTING);
                        break;*/


            } catch(Exception e) {
                Logger.getLogger(MusicPlayer.class, e.getClass().getSimpleName(), e.getMessage()).error();
                //e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        boolean hasArgs = args != null && args.length > 0;
        String fPath =
                hasArgs ? args[0] : "/home/martin/Escritorio/Archivos/Música"
                //"/home/martin/AudioTesting/music/"
                //"/home/martin/AudioTesting/test/mix"
                ;
        MusicPlayer musicPlayer = null;
        try {
            musicPlayer = new MusicPlayer(fPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        musicPlayer.start();

    }
}
