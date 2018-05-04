package org.orangeplayer.audio;

import org.orangeplayer.audio.interfaces.PlayerControls;
import org.orangeplayer.audio.interfaces.PlayerListener;
import org.orangeplayer.thread.PlayerHandler;
import org.orangeplayer.thread.ThreadManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Player extends Thread implements PlayerControls {
    private File rootFolder;
    private Track current;
    private Thread currentThread;

    private ArrayList<String> listSoundPaths;
    private ArrayList<PlayerListener> listListeners;

    private int trackIndex;
    private float currentVolume;
    private boolean on;

    public static float DEFAULT_VOLUME = 80.0f;

    /*private static Player player;

    public static boolean hasInstance() {
        return player != null;
    }

    public static void newInstance(File rootFolder) throws FileNotFoundException {
        player = new Player(rootFolder);
    }

    public static void newInstance(String rootPath) throws FileNotFoundException {
        player = new Player(rootPath);
    }

    public static Player getPlayer() {
        return player;
    }*/

    private Player(File rootFolder) throws FileNotFoundException {
        this.rootFolder = rootFolder;
        listSoundPaths = new ArrayList<>();
        listListeners = new ArrayList<>();
        trackIndex = 0;
        currentVolume = DEFAULT_VOLUME;
        if (!rootFolder.exists())
            throw new FileNotFoundException();
        else {
            loadTracks(rootFolder);
            sortTracks();
        }
        on = false;
        setName("ThreadPlayer "+getId());
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

    private void finishTrack(Track current) {
        if (current != null && !current.isFinished())
            current.finish();
    }

    private void startNewTrackThread() {
        currentThread = new Thread(current);
        currentThread.setName("ThreadTrack: "+current.getTrackFile().getName().substring(0, 10));
        current.setGain(currentVolume);
        currentThread.start();
    }

    private void loadListenerMethod(String methodName, Track track) {
        if (listListeners.isEmpty())
            return;
        int listenerSize = listListeners.size();
        switch (methodName) {
            case "onSongChange":
                for (int i = 0; i < listenerSize; i++)
                    listListeners.get(i).onSongChange(track);
                break;
            case "onPlayed":
                for (int i = 0; i < listenerSize; i++)
                    listListeners.get(i).onPlayed(track);
                break;
            case "onResumed":
                for (int i = 0; i < listenerSize; i++)
                    listListeners.get(i).onResumed(track);
                break;
            case "onPaused":
                for (int i = 0; i < listenerSize; i++)
                    listListeners.get(i).onPaused(track);
                break;
            case "onStarted":
                for (int i = 0; i < listenerSize; i++)
                    listListeners.get(i).onStarted();
                break;
            case "onStopped":
                for (int i = 0; i < listenerSize; i++)
                    listListeners.get(i).onStopped(track);
                break;
            case "onSeeked":
                for (int i = 0; i < listenerSize; i++)
                    listListeners.get(i).onSeeked(track);
                break;
            case "onShutdown":
                for (int i = 0; i < listenerSize; i++)
                    listListeners.get(i).onShutdown();
                break;
        }
    }

    public void addPlayerListener(PlayerListener listener) {
        listListeners.add(listener);
    }

    public void removePlayerListener(PlayerListener reference) {
        for (int i = 0; i < listListeners.size(); i++)
            if (listListeners.get(i).equals(reference))
                listListeners.remove(i);
    }

    public void removeAllListeners() {
        listListeners.clear();
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
        if (current != null) {
            current.play();
            loadListenerMethod("onPlayed", current);
        }
    }

    @Override
    public void pause() {
        if (current != null) {
            current.pause();
            loadListenerMethod("onPaused", current);
        }
    }

    @Override
    public void resumeTrack() {
        if (current != null) {
            current.resumeTrack();
            loadListenerMethod("onResumed", current);
        }
    }

    @Override
    public void stopTrack() {
        if (current != null) {
            current.stopTrack();
            loadListenerMethod("onStopped", current);
        }
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
                loadListenerMethod("onSeeked", current);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void changeSong() {
        this.interrupt();
    }

    @Override
    public synchronized void playNext() {
        Track cur = current;
        current = getNextTrack();
        finishTrack(cur);
        startNewTrackThread();
        loadListenerMethod("onSongChange", current);
        System.out.println(current.getInfoSong());
    }

    public void next() {
        current.finish();
    }

    @Override
    public synchronized void playPrevious() {
        trackIndex-=2;
        if (trackIndex < 0)
            trackIndex = listSoundPaths.size()-1;
        /*Track cur = current;
        current = getPreviousTrack();
        finishTrack(cur);
        startNewTrackThread();
        System.out.println(current.getInfoSong());
        loadListenerMethod("onSongChange", current);*/
        current.finish();
    }

    @Override
    public void shutdown() {
        on = false;
        // Se usa kill porque con finish se cambia la cancion
        if (current != null)
            current.kill();
        // Por sea caso
        System.out.println(currentThread.isAlive());
        ThreadManager.unfreezeThread(this);
        loadListenerMethod("onShutdown", null);
    }

    private synchronized void waitSong() {
        System.out.println("Antes de frezeear");
        //ThreadManager.freezeThread(this);
        ThreadManager.freezeThread(this);
        System.out.println("Frezeeado");
    }

    @Override
    public void run() {
        PlayerHandler.setInstance(this);
        on = true;
        loadListenerMethod("onStarted", null);
        playNext();
        waitSong();
        //System.out.println("Despues de waitSong");
        //System.out.println("Se termino on");
    }

}
