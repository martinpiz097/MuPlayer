package org.orangeplayer.main;

import org.orangeplayer.audio.Player;

import java.io.IOException;
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

        char c;
        String line;

        boolean on = true;

        while (on) {
            try {
                line = scan.nextLine();
                c = line.charAt(0);
                switch (c) {
                    case 'n':
                        player.next();
                        break;
                    case 'p':
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
                }
            }catch(Exception e) {

            }
        }
    }
}
