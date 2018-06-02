package org.muplayer.audio;

import org.muplayer.audio.interfaces.PlayerControls;
import org.muplayer.audio.interfaces.PlayerListener;
import org.muplayer.thread.PlayerHandler;
import org.muplayer.thread.ThreadManager;

import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.muplayer.audio.ListenersNames.*;

public class Player extends Thread implements PlayerControls {
    private File rootFolder;
    private Track current;
    private Thread currentThread;

    private final ArrayList<String> listSoundPaths;
    private final ArrayList<PlayerListener> listListeners;

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

    public Player() throws FileNotFoundException {
        this(null);
    }
    public Player(File rootFolder) throws FileNotFoundException {
        this.rootFolder = rootFolder;
        listSoundPaths = new ArrayList<>();
        listListeners = new ArrayList<>();
        trackIndex = 0;
        currentVolume = DEFAULT_VOLUME;
        on = false;

        if (rootFolder != null) {
            if (!rootFolder.exists())
                throw new FileNotFoundException();
            else {
                loadTracks(rootFolder);
                sortTracks();
            }
            setName("ThreadPlayer "+getId());
        }
    }

    /*public Player(String folderPath) throws FileNotFoundException {
        this(new File(folderPath));
    }*/

    // Problema con ogg al leer tagInfo archivo

    // no se revisaran si los archivos son sonidos por
    // ahora porque se piensa en reducir tiempos de carga
    // haciendo de la revision en tiempo de ejecucion

    private void loadTracks(File folder) {
        File[] fldFiles = folder.listFiles();
        File f = null;
        if (fldFiles != null) {
            for (int i = 0; i < fldFiles.length; i++) {
                f = fldFiles[i];
                if (f.isDirectory())
                    loadTracks(f);
                else
                    listSoundPaths.add(f.getPath());
            }
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
        listSoundPaths.sort(Comparator.naturalOrder());
    }

    private Track getTrack(int index, boolean isNext) {
        Track next = null;
        if (isNext) {
            if (index == getSongsCount()-1)
                index = 0;
            else
                index++;
            for (int i = index; i < listSoundPaths.size(); i++) {
                next = Track.getTrack(listSoundPaths.get(i));
                if (next != null) {
                    // El proximo indice a revisar sera el siguiente
                    // para no devolverse tanto
                    trackIndex = i;
                    break;
                }
            }
            return next;
        }
        else {
            if (index == 0)
                index = getSongsCount()-1;
            else
                index--;

            for (int i = index; i >= 0; i--) {
                next = Track.getTrack(listSoundPaths.get(i));
                if (next != null) {
                    // El proximo indice a revisar sera el siguiente
                    // para no devolverse tanto
                    trackIndex = i;
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
        String trackName = current.getDataSource().getName();
        int strLimit = trackName.length() < 10 ? trackName.length() : 10;
        currentThread.setName("ThreadTrack: "+current.getDataSource().getName().substring(0, strLimit));
        current.setGain(currentVolume);
        currentThread.start();
    }

    private void loadListenerMethod(String methodName, Track track) {
        if (listListeners.isEmpty())
            return;
        int listenerSize = listListeners.size();
        switch (methodName) {
            case ONSONGCHANGE:
                for (int i = 0; i < listenerSize; i++)
                    listListeners.get(i).onSongChange(track);
                break;
            case ONPLAYED:
                for (int i = 0; i < listenerSize; i++)
                    listListeners.get(i).onPlayed(track);
                break;
            case ONRESUMED:
                for (int i = 0; i < listenerSize; i++)
                    listListeners.get(i).onResumed(track);
                break;
            case ONPAUSED:
                for (int i = 0; i < listenerSize; i++)
                    listListeners.get(i).onPaused(track);
                break;
            case ONSTARTED:
                for (int i = 0; i < listenerSize; i++)
                    listListeners.get(i).onStarted();
                break;
            case ONSTOPPED:
                for (int i = 0; i < listenerSize; i++)
                    listListeners.get(i).onStopped(track);
                break;
            case ONSEEKED:
                for (int i = 0; i < listenerSize; i++)
                    listListeners.get(i).onSeeked(track);
                break;
            case ONSHUTDOWN:
                for (int i = 0; i < listenerSize; i++)
                    listListeners.get(i).onShutdown();
                break;
        }
    }

    private synchronized void waitSong() {
        ThreadManager.freezeThread(this);
        System.out.println("Frezeeado");
    }

    /*private synchronized void unfreeze() {
        ThreadManager.unfreezeThread(this);
        System.out.println("Descongelado");
    }*/

    private void shutdownCurrent() {
        if (current != null && (!current.isFinished())) {
            // Cambiar por shutdown el metodo de current o algo parecido
            current.unlinkPlayer();
            current.finish();
        }
    }

    /*private void changeTrack() {
        if (current != null)
            current.finish();
    }*/

    /*private Track getTrackByIndex(int index, boolean prev) {
        if (prev) {
            for (int i = index; i >= 0; i--) {

            }

        }
        else {

        }
    }*/

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
        loadListenerMethod(ONSONGCHANGE, current);
    }

    public boolean hasSounds() {
        return !listSoundPaths.isEmpty();
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
            trackIndex-=(++jumps);
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
    public boolean isMute() {
        return current == null ? true : current.isMute();
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
            final Consumer<File> consumer = sound->{
                if (sound.isDirectory())
                    loadTracks(sound);
                else
                    listSoundPaths.add(sound.getPath());
            };
            final Stream<File> stream = listSounds.stream();
            if (hasSounds()) {
                stream.forEach(consumer);
                sortTracks();
            }
            else {
                suspend();
                stream.forEach(consumer);
                sortTracks();
                resume();
            }


        }

    }

    @Override
    public void addMusic(File musicFolder) {
        this.rootFolder = musicFolder;
        if (hasSounds()) {
            loadTracks(musicFolder);
            sortTracks();
        }
        else {
            suspend();
            loadTracks(musicFolder);
            sortTracks();
            resume();
        }
    }

    public void listTracks() {
        System.out.println("------------------------------");
        if (rootFolder == null)
            System.out.println("Music in folder");
        else
            System.out.println("Music in folder "+rootFolder.getName());
        System.out.println("------------------------------");
        for (int i = 0; i < getSongsCount(); i++)
            System.out.println("Track "+(i+1)+": "
                    +new File(listSoundPaths.get(i)).getName());
        System.out.println("------------------------------");
    }

    public int getTrackProgress() {
        return current.getProgress();
    }

    public int getSongsCount() {
        return listSoundPaths.size();
    }

    public void seekFolder(boolean next) {
        if (current != null) {
            boolean isFolderFound = false;
            String currentParent =
                    current.getDataSource().getParent();
            File nextSound = null;
            int count = 0;

            if (next)
                for (int i = trackIndex+1; i < getSongsCount(); i++) {
                    nextSound = new File(listSoundPaths.get(i));
                    count++;
                    if (!nextSound.getParent().equals(currentParent)) {
                        isFolderFound = true;
                        break;
                    }
                }
            else
                for (int i = trackIndex-1; i >= 0; i--) {
                    nextSound = new File(listSoundPaths.get(i));
                    count--;
                    if (!nextSound.getParent().equals(currentParent)) {
                        isFolderFound = true;
                        currentParent = nextSound.getParent();
                    }
                    // Buscar el primero de la carpeta
                    else if (isFolderFound &&
                            !nextSound.getParent().equals(currentParent)) {
                        nextSound = new File(listSoundPaths.get(i+1));
                        System.out.println("NextSound: "+nextSound.getName());
                        break;
                    }
                }
            if (isFolderFound)
                play(nextSound);
                // Ultima carpeta
            else
                play(new File(next ? listSoundPaths.get(0) :
                        listSoundPaths.get(getSongsCount()-1)));
        }
    }

    @Override
    public void play() {
        if (!isAlive())
            start();
        else if (current != null) {
            current.play();
            loadListenerMethod(ONPLAYED, current);
        }
    }

    // Reproduce archivo de audio en la lista
    @Override
    public void play(File track) {
        int indexOf = listSoundPaths.indexOf(track.getPath());
        if (indexOf != -1) {
            trackIndex = indexOf == 0 ? getSongsCount() :  indexOf-1;
            playNext();
        }
    }

    @Override
    public void play(String trackName) {
        int indexOf = -1;
        for (int i = 0; i < listSoundPaths.size(); i++) {
            if (new File(listSoundPaths.get(i)).getName().equals(trackName)) {
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
            loadListenerMethod(ONPAUSED, current);
        }
    }

    @Override
    public void resumeTrack() {
        if (current != null) {
            current.resumeTrack();
            loadListenerMethod(ONRESUMED, current);
        }
    }

    @Override
    public void stopTrack() {
        if (current != null) {
            current.stopTrack();
            loadListenerMethod(ONSTOPPED, current);
        }
    }

    @Override
    public  void finish() {
        shutdown();
    }

    @Override
    public void seek(int seconds) {
        if (current != null) {
            try {
                current.seek(seconds);
                loadListenerMethod(ONSEEKED, current);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public float getGain() {
        return current == null ? -1 : current.getGain();
    }

    // 0-100
    @Override
    public void setGain(float volume) {
        if (current != null) {
            current.setGain(volume);
            currentVolume = volume < 0 ? 0 : (volume > 100 ? 100 : volume);
        }
    }

    @Override
    public void mute() {
        if (current != null)
            current.mute();
    }

    @Override
    public void unmute() {
        if (current != null)
            current.unmute();
    }

    @Override
    public synchronized void playNext() {
        if (current != null)
            current.finish();
        else
            current = getNextTrack();
    }

    @Override
    public synchronized void playPrevious() {
        Track cur = current;
        current = getPreviousTrack();
        if (cur != null) {
            cur.unlinkPlayer();
            finishTrack(cur);
        }
        startNewTrackThread();
        System.out.println(current.getInfoSong());
        loadListenerMethod(ONSONGCHANGE, current);
    }

    @Override
    public void shutdown() {
        on = false;
        // Se usa kill porque con finish se cambia la cancion
        System.out.println(currentThread.isAlive());
        System.out.println("Antes de apagar current");
        shutdownCurrent();
        System.out.println("Antes de descongelar player");
        this.interrupt();
        System.out.println("Player descongelado");
        loadListenerMethod(ONSHUTDOWN, null);
    }

    @Override
    public void run() {
        PlayerHandler.setInstance(this);
        on = true;
        loadListenerMethod(ONSTARTED, null);
        loadNextTrack();
        waitSong();
    }

}
