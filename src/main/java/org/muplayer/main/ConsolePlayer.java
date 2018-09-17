package org.muplayer.main;

import org.muplayer.audio.Player;
import org.muplayer.system.Logger;

import java.io.File;
import java.io.FileNotFoundException;

public class ConsolePlayer extends Thread {
    private volatile Player player;
    private volatile CommandInterpreter interpreter;

    public ConsolePlayer(String folder) throws FileNotFoundException {
        player = new Player(folder);
        initInterpreter();
        setName("ConsolePlayer");
    }

    private void initInterpreter() {
        interpreter = cmd -> {
            final String cmdOrder = cmd.getOrder();

            switch (cmdOrder) {
                case ConsoleOrder.START:
                    if (player.isAlive() && cmd.hasOptions()) {
                        File musicFolder = new File(cmd.getOptionAt(0));
                        if (musicFolder.exists()) {
                            Player newPlayer = new Player(musicFolder);
                            player.shutdown();
                            newPlayer.start();
                            player = newPlayer;
                        }
                        else
                            Logger.getLogger(this, "Folder not exists").rawError();
                    }
                    else if(!player.isAlive())
                        player.start();

                    break;
                case ConsoleOrder.PLAY:
                    player.resumeTrack();
                    break;
            }

        };
    }
}
