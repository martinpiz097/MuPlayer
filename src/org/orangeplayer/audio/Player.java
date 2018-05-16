package org.orangeplayer.audio;

import org.orangeplayer.audio.interfaces.PlayerControls;
import org.orangeplayer.audio.interfaces.PlayerListener;
import org.orangeplayer.thread.PlayerHandler;
import org.orangeplayer.thread.ThreadManager;

import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Player extends Thread implements PlayerControls {
    private File rootFolder;
    private Track current;
    private Thread currentThread;

    private final ArrayList<String> listSoundPaths;
    private final ArrayList<PlayerListener> listListeners;

    private int trackIndex;
    private float currentVolume;
    private boolean on;
    //private boolean hasSounds;

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

    public Player() {
        this.rootFolder = rootFolder;
        listSoundPaths = new ArrayList<>();
        listListeners = new ArrayList<>();
        trackIndex = 0;
        currentVolume = DEFAULT_VOLUME;
        on = false;
        //hasSounds = false;
        setName("ThreadPlayer "+getId());
    }
    public Player(File rootFolder) throws FileNotFoundException {
        this.rootFolder = rootFolder;
        listSoundPaths = new ArrayList<>();
        listListeners = new ArrayList<>();
        trackIndex = 0;
        currentVolume = DEFAULT_VOLUME;
        on = false;
        //hasSounds = false;
        if (!rootFolder.exists())
            throw new FileNotFoundException();
        else {
            loadTracks(rootFolder);
            sortTracks();
        }
        setName("ThreadPlayer "+getId());
    }

    public Player(String folderPath) throws FileNotFoundException {
        this(new File(folderPath));
    }

    // Problema con ogg al leer tagInfo archivo

    // no se revisaran si los archivos son sonidos por
    // ahora porque se piensa en reducir tiempos de carga
    // haciendo de la revision en tiempo de ejecucion

    private void loadTracks(File folder) {
        File[] fldFiles = folder.listFiles();
        File f = null;
        if (fldFiles != null)
            for (int i = 0; i < fldFiles.length; i++) {
                f = fldFiles[i];
                if (f.isDirectory())
                    loadTracks(f);
                else
                    listSoundPaths.add(f.getPath());
        }
    }

    private void loadTracks(List<File> listFiles) {
        trackIndex = 0;
        listFiles.stream().forEach(f->{
            if (f.isDirectory())
                loadTracks(f);
            else
                listSoundPaths.add(f.getPath());
        });
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
        String trackName = current.getTrackFile().getName();
        int strLimit = trackName.length() < 10 ? trackName.length() : 10;
        currentThread.setName("ThreadTrack: "+current.getTrackFile().getName().substring(0, strLimit));
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

    private synchronized void waitSong() {
        ThreadManager.freezeThread(this);
        System.out.println("Frezeeado");
    }

    private void shutdownPlaying() {
        if (current != null && (!current.isKilled() && !current.isKilled()))
            // Cambiar por shutdown el metodo de current o algo parecido
            current.kill();
    }

    private void changeTrack() {
        if (current != null)
            current.finish();
    }

    private void waitForSongs() {
        while (on && getSongsCount() == 0);
    }

    void loadNextTrack() {
        // Se debe verificar que no es un archivo de audio porque
        // cuando solo hay archivos que no son audio se lanza un
        // nullpointerexception

        waitForSongs();
        Track cur = current;
        current = getNextTrack();
        finishTrack(cur);
        if (current != null) {
            startNewTrackThread();
            System.out.println("Song: "+trackIndex);
            System.out.println(current.getInfoSong());
        }
        loadListenerMethod("onSongChange", current);
    }

    public void analyzeFiles() {
        int tracksSize = listSoundPaths.size();
        List<String> listAnalyzed = (
                listSoundPaths.getClass().isInstance(LinkedList.class)?
                    new LinkedList<>() : new ArrayList<>());
        listSoundPaths.stream().forEach((fsound)->{
            if (Track.isValidTrack(fsound))
                listAnalyzed.add(fsound);
        });
        listSoundPaths.clear();
        listSoundPaths.addAll(listAnalyzed);
    }

    // Test
    public void jumpTrack(int jumps) {
        if (jumps > 0)
            trackIndex+=(--jumps);
        else
            trackIndex-=jumps;
        if (trackIndex >= listSoundPaths.size())
            trackIndex = 0;
        else if (trackIndex < 0)
            trackIndex = listSoundPaths.size()-1;
    }

    public void addPlayerListener(PlayerListener listener) {
        listListeners.add(listener);
    }

    public ArrayList<PlayerListener> getListeners() {
        return listListeners;
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

    public synchronized SourceDataLine getTrackLine() {
        if (current == null)
            System.out.println("Current is null");
        else {
            System.out.println("CurrentLine: "+current.getTrackLine());
        }
        return current == null ? null :
                (current.getTrackLine() == null ?
                        null : current.getTrackLine().getDriver());
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
    public void open(File sound) {
        listSoundPaths.clear();
        listSoundPaths.add(sound.getPath());
        if (isPlaying())
            playNext();
        else if (isAlive())
            play();
        else
            start();
    }

    @Override
    public void open(List<File> listSounds) {
        listSounds.clear();
        loadTracks(listSounds);
        sortTracks();
        /*
        // Ver si el thread no es nulo
        if (current != null && currentThread.isAlive()) {
            current.kill();
            current = getTrack(trackIndex, true);
            currentThread = new Thread(current);
            currentThread.start();
        }
        */
    }

    @Override
    public void addMusic(List<File> listSounds) {
        if (!listSounds.isEmpty()) {
            if (listSounds instanceof LinkedList) {
                listSounds.stream().forEach(sound->{
                    if (sound.isDirectory())
                        loadTracks(sound);
                    else
                        listSoundPaths.add(sound.getPath());
                });
            }
            else {
                File sound = null;
                for (int i = 0; i < listSounds.size(); i++) {
                    sound = listSounds.get(i);
                    if (sound.isDirectory())
                        loadTracks(sound);
                    else
                        listSoundPaths.add(sound.getPath());
                }
            }
        }
        sortTracks();    }

    @Override
    public void addMusic(File musicFolder) {
        loadTracks(musicFolder);
        sortTracks();
    }

    public int getTrackProgress() {
        return current.getProgress();
    }

    public int getSongsCount() {
        return listSoundPaths.size();
    }

    @Override
    public void play() {
        if (!isAlive())
            start();
        else if (current != null) {
            current.play();
            loadListenerMethod("onPlayed", current);
        }
    }

    // Reproduce archivo de audio en la lista
    public void play(File track) {
        int indexOf = listSoundPaths.indexOf(track.getPath());
        if (indexOf != -1) {
            trackIndex = indexOf == 0 ? getSongsCount() :  indexOf-1;
            playNext();
        }
    }

    public void play(String trackName) {
        int indexOf = -1;
        for (int i = 0; i < listSoundPaths.size(); i++) {
            if (new File(listSoundPaths.get(i))
                    .getName().equals(trackName)) {
                indexOf = i;
                break;
            }
        }

        if (indexOf != -1) {
            trackIndex = indexOf == 0 ? getSongsCount() :  indexOf-1;
            playNext();
        }
    }

    @Override
    public void pause() {
        if (current != null) {
            current.pause();
            //loadListenerMethod("onPaused", current);
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
    public void seek(int seconds) {
        if (current != null) {
            try {
                current.seek(seconds);
                loadListenerMethod("onSeeked", current);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void playNext() {
        changeTrack();
    }

    @Override
    public synchronized void playPrevious() {
        trackIndex-=2;
        if (trackIndex < 0)
            trackIndex = listSoundPaths.size()-1;
        changeTrack();
        /*Track cur = current;
        current = getPreviousTrack();
        finishTrack(cur);
        startNewTrackThread();
        System.out.println(current.getInfoSong());
        loadListenerMethod("onSongChange", current);*/
    }

    @Override
    public void shutdown() {
        on = false;
        // Se usa kill porque con finish se cambia la cancion
        if (current != null)
            current.kill();
        // Por sea caso
        System.out.println(currentThread.isAlive());
        System.out.println("Antes de apagar current");
        shutdownPlaying();
        System.out.println("Antes de descongelar player");
        this.interrupt();
        System.out.println("Player descongelado");
        loadListenerMethod("onShutdown", null);
    }

    @Override
    public void run() {
        PlayerHandler.setInstance(this);
        on = true;
        loadListenerMethod("onStarted", null);
        loadNextTrack();
        waitSong();
        //System.out.println("Despues de waitSong");
        //System.out.println("Se termino on");
    }

}
