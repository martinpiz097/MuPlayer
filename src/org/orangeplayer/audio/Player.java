package org.orangeplayer.audio;

import org.orangeplayer.audio.org.orangeplayer.audio.interfaces.MusicControls;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Player extends Thread implements MusicControls {
    private File rootFolder;
    private Track current;
    private Thread currentThread;
    private ArrayList<String> listSoundPaths;

    private int trackIndex;

    public Player(File rootFolder) {
        this.rootFolder = rootFolder;
        listSoundPaths = new ArrayList<>();
        trackIndex = 0;
        System.out.println("Loading Tracks.....");
        loadTracks(rootFolder);
        sortTracks();
        System.out.println("Tracks Loaded!");
    }

    public Player(String folderPath) {
        this(new File(folderPath));
    }

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
        //listSoundPaths.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        listSoundPaths.sort((o1, o2) -> o2.compareTo(o1));
    }

    private Track getTrack(int index) {
        Track next = null;
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

    private Track getTrackPrev(int index) {
        Track next = null;
        index-=2;

        if (index == -1)
            index = listSoundPaths.size()-1;
        // Revisar for para despues unir en el otro metodo
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


    private Track getNextTrack() {
        return getTrack(trackIndex);
    }

    private Track getPreviousTrack() {
        return getTrackPrev(trackIndex);
    }

    public void playNext() {
        if (current != null) {
            current.finish();
            if (currentThread != null) {
                currentThread.stop();
                currentThread = null;
            }
        }
        current = getNextTrack();
        currentThread = new Thread(current);
        currentThread.start();
    }

    public void playPrevious() {
        if (current != null && !current.isFinished()) {
            current.finish();
        }
        current = getPreviousTrack();
        new Thread(current).start();
        current.play();
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
            current.resume();
    }

    @Override
    public void stopTrack() {
        if (current != null)
            current.stop();
    }

    @Override
    public void finish() {
        if (current != null)
            current.finish();
    }

    @Override
    public void setGain(float volume) {
        if (current != null)
            current.setGain(volume);
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
    public void run() {
        while (true) {
            playNext();
            System.out.println(current.isFinished());
            while (!current.isFinished()){}
            System.out.println(current.isFinished());
            System.out.println("------------------");
        }

    }

    public static void main(String[] args) throws IOException {
        Player player = new Player("/home/martin/AudioTesting/music/");
        player.start();
        Scanner scan = new Scanner(System.in);

        char c;
        String line;

        while (true) {
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
                    case 'k':
                        player.seek(Integer.parseInt(line.split(" ")[1]));
                }
            }catch(Exception e) {

            }
        }

    }

}
