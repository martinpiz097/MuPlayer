package org.orangeplayer.audio;

import java.io.File;

public class Player extends Thread {
    private File rootFolder;
    //private LinkedList<Track> listTracks;

    // No se usara lista para ahorrar ram


    public Player(File rootFolder) {
        this.rootFolder = rootFolder;
    }

    @Override
    public void run() {
        
    }

}
