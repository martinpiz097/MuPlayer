package org.orangeplayer.audio;

import org.orangeplayer.audio.interfaces.PlayerControls;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Player extends Thread implements PlayerControls {
    private File rootFolder;
    private Track current;
    private Thread currentThread;
    private ArrayList<String> listSoundPaths;

    private int trackIndex;
    private boolean on;

    private float currentVolume;
    public Player(File rootFolder) throws FileNotFoundException {
        this.rootFolder = rootFolder;
        listSoundPaths = new ArrayList<>();
        trackIndex = 0;
        on = false;
        currentVolume = 80;
        if (!rootFolder.exists())
            throw new FileNotFoundException();
        else {
            loadTracks(rootFolder);
            sortTracks();
        }
    }

    public Player(String folderPath) throws FileNotFoundException {
        this(new File(folderPath));
    }

    // Problema con ogg al leer info archivo

    // no se revisaran si los archivos son sonidos por
    // ahora porque se piensa en reducir tiempos de carga
    // haciendo de la revision en tiempo de ejecucion

    private void loadTracks(File folder) {
        File[] fldFiles = folder.listFiles();
        File f;
        if (fldFiles != null)
            for (int i = 0; i < fldFiles.length; i++) {
                f = fldFiles[i];
                if (f.isDirectory())
                    loadTracks(f);
                else
                    listSoundPaths.add(f.getPath());
        }
    }

    // Agregar opcion para ordenar

    private void sortTracks() {
        listSoundPaths.sort((o1, o2) -> o1.compareTo(o2));
        //listSoundPaths.sort((o1, o2) -> o2.compareTo(o1));
    }

    private Track getTrack(int index, boolean isNext) {
        Track next = null;
        if (isNext) {
            if (index == listSoundPaths.size())
                index = 0;
            for (int i = index; i < listSoundPaths.size(); i++) {
                next = Track.getTrack(listSoundPaths.get(i));
                if (next != null) {
                    // El proximo indice a revisar sera el siguiente
                    // para no devolverse tanto
                    trackIndex = i+1;
                    break;
                }
            }
            return next;
        }
        else {
            index-=2;

            if (index == -1)
                index = listSoundPaths.size()-1;

            for (int i = index; i >= 0; i--) {
                next = Track.getTrack(listSoundPaths.get(i));
                if (next != null) {
                    // El proximo indice a revisar sera el siguiente
                    // para no devolverse tanto
                    trackIndex = i+1;
                    break;
                }
            }
            return next;
        }
    }

    private Track getNextTrack() {
        return getTrack(trackIndex, true);
    }

    private Track getPreviousTrack() {
        return getTrack(trackIndex, false);
    }

    private void finishCurrent(Track current) {
        if (current != null && !current.isFinished())
            current.finish();
    }

    private void startNewThread() {
        currentThread = new Thread(current);
        currentThread.setName("ThreadTrack: "+current.getTrackFile().getName().substring(0, 10));
        current.setGain(currentVolume);
        currentThread.start();
    }

    // Waiting for testing
    public void reloadTracks() {
        listSoundPaths.clear();
        loadTracks(rootFolder);
        sortTracks();
    }

    public Track getCurrent() {
        return current;
    }

    @Override
    public boolean isPlaying() {
        return current == null ? false : current.isPlaying();
    }


    @Override
    public boolean isPaused() {
        return current == null ? false : current.isPaused();
    }

    @Override
    public boolean isStoped() {
        return current == null ? false : current.isStoped();
    }

    @Override
    public boolean isFinished() {
        return current == null ? false : current.isFinished();
    }

    @Override
    public void play() {
        if (current != null)
            current.play();
    }

    @Override
    public void pause() {
        if (current != null)
            current.pause();
    }

    @Override
    public void resumeTrack() {
        if (current != null)
            current.resumeTrack();
    }

    @Override
    public void stopTrack() {
        if (current != null)
            current.stopTrack();
    }

    @Override
    public  void finish() {
        shutdown();
    }

    // 0-100
    @Override
    public void setGain(float volume) {
        if (current != null) {
            current.setGain(volume);
            currentVolume = volume;
        }
    }

    @Override
    public void seek(int bytes) {
        if (current != null) {
            try {
                current.seek(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void playNext() {
        Track cur = current;
        current = getNextTrack();
        finishCurrent(cur);
        startNewThread();
        System.out.println(current.getInfoSong());
    }

    @Override
    public void playPrevious() {
        Track cur = current;
        current = getPreviousTrack();
        finishCurrent(cur);
        startNewThread();
        System.out.println(current.getInfoSong());
    }

    @Override
    public void shutdown() {
        on = false;
        if (current != null)
            current.finish();
    }

    @Override
    public void run() {
        on = true;
        playNext();
        while (on) {
            if (current.isFinished()) {
                playNext();
                System.out.println("IF Play Next");
                //System.out.println("PlayNext");
            }
            //System.out.println(current.isFinished());
            //System.out.println("------------------");
        }
        System.out.print("");
    }



    public static void main(String[] args) throws IOException {
        boolean hasArgs = args != null && args.length > 0;
        String fPath = hasArgs ? args[0] : "/home/martin/AudioTesting/musictest/";

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
                        player.playNext();
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
