package org.orangeplayer.ontesting;

import org.orangeplayer.audio.Player;

import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class TestPlayer {
    public static void main(String[] args) throws IOException {
        boolean hasArgs = args != null && args.length > 0;
        String fPath = hasArgs ? args[0] : "/home/martin/AudioTesting/audio";

        // Ver validacion de archivos de audio y que hacer cuando
        // la carpeta esta vacia al cargar la carpeta para evitar
        // un buble infinito cuando se lee una carpeta sin archivos de audio

        //Player.newInstance(fPath);
        //Player player = Player.getPlayer();
        Player player = new Player();
        player.start();
        player.addMusic(new File(fPath));
        //player.analyzeFiles();

        System.out.println("Sounds total: "+player.getSongsCount());

        Scanner scan = new Scanner(System.in);
        SourceDataLine trackLine = player.getTrackLine();

        char c;
        String line;

        boolean on = true;

        while (on) {
            try {
                line = scan.nextLine();
                c = line.charAt(0);
                switch (c) {
                    case 'n':
                        if (line.length() >= 3)
                            player.jumpTrack(Integer.parseInt(line.substring(2)));
                        player.playNext();
                        System.err.println("Antes de trackLine");
                        trackLine = player.getTrackLine();
                        System.out.println("TrackLine: "+trackLine);
                        if (trackLine != null) {
                            System.err.println("Antes de controls");
                            System.out.println("Controls: "+Arrays.toString(trackLine.getControls()));
                            Control pan = trackLine.getControl(FloatControl.Type.PAN);
                            System.out.println("PAN: "+pan);
                        }
                        break;

                    case 'p':
                        if (line.length() >= 3)
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
                }
            } catch (IllegalArgumentException e1) {
                System.err.println("Control no soportado");
            } catch(Exception e2) {
                System.err.println("Exception: "+e2.getMessage());
                System.err.println("Cause: "+e2.toString());
                e2.printStackTrace();
            }
        }
    }
}
