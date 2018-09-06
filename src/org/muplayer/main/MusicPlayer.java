package org.muplayer.main;

import org.muplayer.audio.Player;
import org.muplayer.system.Logger;

import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class MusicPlayer extends Thread {

    private volatile Player player;

    public MusicPlayer(String folderPath) throws FileNotFoundException {
        this(new File(folderPath));
    }

    public MusicPlayer(File folder) throws FileNotFoundException {
        this.player = new Player(folder);
    }

    @Override
    public void run() {
        System.out.println("Sounds total: "+player.getSongsCount());
        player.start();

        Scanner scan = new Scanner(System.in);
        SourceDataLine playerLine;

        char c;
        String line;

        boolean on = true;

        while (on) {
            try {
                line = scan.nextLine().trim();
                c = line.charAt(0);
                switch (c) {
                    case 'n':
                        if (line.length() == 2 && line.charAt(1) == 'f')
                            player.seekFolder(Player.SeekOption.NEXT);
                        else {
                            if (line.length() >= 3)
                                player.jumpTrack(Integer.parseInt(line.substring(2)));
                            player.playNext();
                            System.err.println("Antes de playerLine");
                            playerLine = player.getTrackLine();
                            System.err.println("TrackLineNull: "+
                                    (playerLine == null ? "Yes":"No"));
                            /*if (playerLine != null) {
                                System.err.println("Antes de controls");
                                System.out.println("Controls: "+ Arrays.toString(playerLine.getControls()));
                                Control pan = playerLine.getControl(FloatControl.Type.PAN);
                                System.out.println("PAN: "+pan);
                            }*/
                        }
                        break;

                    case 'p':
                        if (line.length() == 2 && line.charAt(1) == 'f')
                            player.seekFolder(Player.SeekOption.PREV);
                        else if (line.length() >= 3)
                            player.jumpTrack((Integer.parseInt(line.substring(2))) * -1);
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
                        System.out.println(player.getTrackProgress());
                        break;
                    case 'g':
                        player.getCurrent().gotoSecond(
                                Integer.parseInt(line.substring(2).trim()));
                        break;
                    case 'c':
                        System.out.println(player.getSongsCount());
                        break;
                    case 'l':
                        if (line.length() == 2 && line.charAt(1) == 'f')
                            player.printFolders();
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
                        System.out.println(player.getCurrent().getFormat());
                        break;
                    case 'i':
                        player.getCurrent().getLineInfo();
                        break;
                    case 'ñ':
                        player.play();
                        break;
                    /*case 'o':
                        //AudioFileFormat format = AudioSystem.getAudioFileFormat(player.getCurrent().getDataSource());
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


            } catch (IllegalArgumentException e) {
                Logger.getLogger(MusicPlayer.class, "Exception: "+e.getMessage()).rawError();
            } catch(Exception e) {
                Logger.getLogger(MusicPlayer.class, "Exception: "+e.getMessage()).rawError();
                Logger.getLogger(MusicPlayer.class, "Cause: "+e.getCause()).rawError();
                //e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        boolean hasArgs = args != null && args.length > 0;
        String fPath =
                hasArgs ? args[0] : "/home/martin/Escritorio/Archivos/Música"
                //"/home/martin/AudioTesting/music/"
                ;
        MusicPlayer musicPlayer = new MusicPlayer(fPath);
        musicPlayer.start();

    }
}
