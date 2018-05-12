package org.orangeplayer.main2;

import org.orangeplayer.audio.Player;

import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class TestPlayer {
    public static void main(String[] args) throws IOException {
        boolean hasArgs = args != null && args.length > 0;
        String fPath = hasArgs ? args[0] : "/home/martin/AudioTesting/music/";

        //Player.newInstance(fPath);
        //Player player = Player.getPlayer();
        Player player = new Player(fPath);
        player.start();
        Scanner scan = new Scanner(System.in);
        // /home/martin/AudioTesting/music/Alejandro Silva/1 - 1999/AlbumArtSmall.jpg
        // /home/martin/AudioTesting/music/NSYNC/NSYNC - No Strings Attached (2000)/ReadMe.txt

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
                        System.err.println("Antes de controls");
                        System.out.println("Controls: "+Arrays.toString(trackLine.getControls()));
                        Control pan = trackLine.getControl(FloatControl.Type.PAN);
                        System.out.println("PAN: "+pan);

                        //System.out.println("Sample Rate: " + sample);
                        //System.out.println("Volume: " + volume);

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
                        player.setGain(Float.parseFloat(line.split(" ")[1]));
                        break;
                    case 'k':
                        player.seek(Integer.parseInt(line.split(" ")[1]));
                        break;
                    case 'e':
                        on = false;
                        player.shutdown();
                        break;
                    case 'u':
                        player.reloadTracks();
                        break;
                    case 'w':
                        System.out.println(player.getCurrentProgress());
                        break;
                    case 'g':
                        player.getCurrent().gotoSecond(
                                Integer.parseInt(line.substring(2).trim()));
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
