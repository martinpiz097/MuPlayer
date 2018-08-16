package org.muplayer.audio;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.muplayer.audio.interfaces.PlayerControls;
import org.muplayer.audio.interfaces.PlayerListener;
import org.muplayer.system.Logger;
import org.muplayer.system.TrackStates;
import org.muplayer.thread.PlayerHandler;
import org.muplayer.thread.ThreadManager;

import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.muplayer.system.ListenersNames.*;

public class Player extends Thread implements PlayerControls {
    private File rootFolder;
    private volatile Track current;
    private volatile Thread currentThread;

    private final ArrayList<String> listSoundPaths;
    private final ArrayList<String> listFolderPaths;
    private final ArrayList<PlayerListener> listListeners;

    private int trackIndex;
    private float currentVolume;
    private boolean on;

    public static float DEFAULT_VOLUME = 85.0f;

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
        this(null);
    }
    public Player(File rootFolder) {
        this.rootFolder = rootFolder;
        listSoundPaths = new ArrayList<>();
        listFolderPaths = new ArrayList<>();
        listListeners = new ArrayList<>();
        trackIndex = -1;
        currentVolume = DEFAULT_VOLUME;
        on = false;

        if (rootFolder != null) {
            if (!rootFolder.exists())
                rootFolder.mkdirs();
                //throw new FileNotFoundException();
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

            boolean hasFiles = false;
            for (int i = 0; i < fldFiles.length; i++) {
                f = fldFiles[i];
                if (f.isDirectory())
                    loadTracks(f);
                else {
                    listSoundPaths.add(f.getPath());
                    hasFiles = true;
                }
            }
            if (folder != rootFolder && hasFiles)
                try {
                    listFolderPaths.add(folder.getCanonicalPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void loadTracks(List<File> listFiles) {
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
        listFolderPaths.sort(Comparator.naturalOrder());

        /*listFolderPaths.forEach(fp->{
            System.out.println(new File(fp).getName());
        });

        System.exit(0);*/
    }

    private int getFolderIndex() {
        String currentParent = current != null ? current.getDataSource().getParent() : null;
        if (currentParent != null) {
            for (int i = 0; i < listFolderPaths.size(); i++) {
                System.out.println(listFolderPaths.get(i));
                if (listFolderPaths.get(i).equals(currentParent))
                    return i;
            }
        }
        return -1;
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
            if (next != null)
                next.setGain(currentVolume);
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
            next.setGain(currentVolume);
            return next;
        }
    }

    private void updateTrack(boolean next) {
        current = getTrack(trackIndex, next);
    }

    private void finishTrack(Track current) {
        if (current != null && !current.isFinished())
            current.finish();
    }

    private void startNewTrackThread() {
        currentThread = new Thread(current);
        final String trackName = current.getDataSource().getName();
        final int strLimit = trackName.length() < 10 ? trackName.length() : 10;
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

    private synchronized void freezePlayer() {
        ThreadManager.freezeThread(this);
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
        updateTrack(true);
        if (cur != null && !cur.isFinished())
            cur.finish();
        if (current != null)
            startNewTrackThread();
    }

    public synchronized boolean hasSounds() {
        return !listSoundPaths.isEmpty();
    }

    public byte getCurrentTrackState() {
        return current == null ? TrackStates.UNKNOWN : current.getState();
    }

    public String getCurrentTrackStateToString() {
        return current == null ? "Unknown" : current.getStateToString();
    }

    public int getFoldersCount() {
        return listFolderPaths.size();
    }

    public ArrayList<String> getListFolderPaths() {
        return listFolderPaths;
    }

    public synchronized ArrayList<PlayerListener> getListListeners() {
        return listListeners;
    }

    public synchronized void analyzeFiles() {
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

        listSoundPaths.sort(Comparator.naturalOrder());

        int folderSize = listFolderPaths.size();
        for (int i = 0; i < folderSize; i++) {
            int finalI = i;
            if (!listSoundPaths.parallelStream().anyMatch(sp->
                    new File(sp).getParent().equals(listFolderPaths.get(finalI)))) {
                listFolderPaths.remove(i);
                folderSize--;
            }
        }

    }

    // Test
    public synchronized void jumpTrack(int jumps) {
        if (jumps > 0)
            trackIndex+=(--jumps);
        else
            trackIndex-=(++jumps);
        if (trackIndex >= listSoundPaths.size())
            trackIndex = 0;
        else if (trackIndex < 0)
            trackIndex = listSoundPaths.size()-1;
    }

    public synchronized ArrayList<String> getListSoundPaths() {
        return listSoundPaths;
    }

    // Se supone que todos los tracks serian validos
    // sino rescatar de los que sean no mas
    public synchronized ArrayList<AudioTag> getTrackTags() {
        ArrayList<AudioTag> listTags = new ArrayList<>();

        AudioTag tag;
        for (int i = 0; i < listSoundPaths.size(); i++) {
            try {
                tag = new AudioTag(listSoundPaths.get(i));
                if (tag.isValidFile())
                    listTags.add(tag);
            } catch (ReadOnlyFileException | IOException |
                    InvalidAudioFrameException | TagException | CannotReadException e) {
            }
        }
        return listTags;
    }


    public synchronized void addPlayerListener(PlayerListener listener) {
        listListeners.add(listener);
    }

    public synchronized ArrayList<PlayerListener> getListeners() {
        return listListeners;
    }

    public synchronized void removePlayerListener(PlayerListener reference) {
        for (int i = 0; i < listListeners.size(); i++)
            if (listListeners.get(i).equals(reference))
                listListeners.remove(i);
    }

    public synchronized void removeAllListeners() {
        listListeners.clear();
    }

    // Waiting for testing
    public void reloadTracks() {
        listSoundPaths.clear();
        loadTracks(rootFolder);
        sortTracks();
    }

    public synchronized Track getCurrent() {
        return current;
    }

    public synchronized SourceDataLine getTrackLine() {
        return current == null ? null :
                (current.getTrackLine() == null ?
                        null : current.getTrackLine().getDriver());
    }

    @Override
    public synchronized boolean isPlaying() {
        return current == null ? false : current.isPlaying();
    }


    @Override
    public synchronized boolean isPaused() {
        return current == null ? false : current.isPaused();
    }

    @Override
    public synchronized boolean isStopped() {
        return current == null ? false : current.isStopped();
    }

    @Override
    public synchronized boolean isFinished() {
        return current == null ? false : current.isFinished();
    }

    @Override
    public synchronized boolean isMute() {
        return current == null ? true : current.isMute();
    }

    @Override
    public synchronized void open(File sound) {
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
    public synchronized void open(List<File> listSounds) {
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
    public synchronized void addMusic(List<File> listSounds) {
        if (!listSounds.isEmpty()) {
            final Consumer<File> consumer = sound->{
                if (sound.isDirectory())
                    loadTracks(sound);
                else {
                    listSoundPaths.add(sound.getPath());
                }
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
    public synchronized void addMusic(File musicFolder) {
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

    public synchronized void listTracks() {
        Logger.getLogger(this, "------------------------------").rawInfo();
        if (rootFolder == null)
            Logger.getLogger(this, "Music in folder").rawInfo();
        else
            Logger.getLogger(this, "Music in folder "+rootFolder.getName()).rawInfo();
        Logger.getLogger(this, "------------------------------").rawInfo();
        for (int i = 0; i < getSongsCount(); i++)
            Logger.getLogger(this, "Track "+(i+1)+": "
                    +new File(listSoundPaths.get(i)).getName()).rawInfo();
        Logger.getLogger(this, "------------------------------").rawInfo();

    }

    public synchronized int getTrackProgress() {
        return current.getProgress();
    }

    public synchronized int getSongsCount() {
        return listSoundPaths.size();
    }

    public synchronized void seekFolder(boolean next) {
        seekFolder(next, 1);
    }

    public synchronized void seekFolder(boolean next, int jumps) {
        int folderIndex = getFolderIndex();
        Logger.getLogger(this, "Current FolderIndex en seekFolder: "+ folderIndex).rawInfo();
        Logger.getLogger(this, "Current Parent en seekFolder: "+ current.getDataSource().getParent()).rawInfo();
        if (folderIndex != -1) {
            int newIndex;

            if (next) {
                newIndex = folderIndex+jumps;

                if (newIndex >= getFoldersCount()) {
                    trackIndex = -1;
                    playNext();
                }
                else {
                    int currentJumps = 0;
                    File fileSong = null;
                    for (int i = trackIndex+1; i < getSongsCount(); i++) {
                        fileSong = new File(listSoundPaths.get(i));
                        if (!fileSong.getParent().equals(current.getDataSource().getParent())) {
                            currentJumps++;
                        }
                        if (jumps == currentJumps) {
                            play(fileSong);
                            break;
                        }
                    }
                }
            }

            else {
                int finalFolderIndex = folderIndex - jumps;

                if (finalFolderIndex < 0) {
                    trackIndex = 0;
                    playPrevious();
                }
                else {
                    File fileSong = null;
                    File folderToFind = new File(listFolderPaths.get(finalFolderIndex));

                    for (int i = trackIndex-1; i >= 0; i--) {
                        fileSong = new File(listSoundPaths.get(i));
                        if (fileSong.getParent().equals(folderToFind.getParent()))
                            break;
                    }
                    if (fileSong != null)
                        play(fileSong);
                }
            }
        }
    }

    @Override
    public synchronized void play() {
        if (!isAlive())
            start();
        else if (current != null) {
            current.play();
            loadListenerMethod(ONPLAYED, current);
        }
    }

    // Reproduce archivo de audio en la lista
    // (is alive)
    @Override
    public synchronized void play(File track) {
        int indexOf = listSoundPaths.indexOf(track.getPath());
        if (indexOf != -1) {
            trackIndex = indexOf;
            if (current != null)
                current.kill();
            current = Track.getTrack(track);
            startNewTrackThread();
        }
    }

    @Override
    public synchronized void play(String trackName) {
        int indexOf = -1;
        File song = null;

        for (int i = 0; i < listSoundPaths.size(); i++) {
            song = new File(listSoundPaths.get(i));
            if (song.getName().equals(trackName)) {
                indexOf = i;
                break;
            }
            song = null;
        }

        if (indexOf != -1) {
            trackIndex = indexOf;
            if (current != null)
                current.kill();
            current = Track.getTrack(song);
            startNewTrackThread();
        }
    }

    @Override
    public synchronized void pause() {
        if (current != null) {
            current.pause();
            loadListenerMethod(ONPAUSED, current);
        }
    }

    @Override
    public synchronized void resumeTrack() {
        if (current != null) {
            current.resumeTrack();
            loadListenerMethod(ONRESUMED, current);
        }
    }

    @Override
    public synchronized void stopTrack() {
        if (current != null) {
            current.stopTrack();
            loadListenerMethod(ONSTOPPED, current);
        }
    }

    @Override
    public synchronized void finish() {
        shutdown();
    }

    @Override
    public synchronized void seek(int seconds) {
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
    public synchronized float getGain() {
        return current == null ? -1 : current.getGain();
    }

    // 0-100
    @Override
    public synchronized void setGain(float volume) {
        if (current != null) {
            current.setGain(volume);
            currentVolume = current.getGain();
        }
    }

    @Override
    public synchronized void mute() {
        if (current != null) {
            current.mute();
            currentVolume = 0;
        }
    }

    @Override
    public synchronized void unmute() {
        if (current != null) {
            current.unmute();
            currentVolume = current.getGain();
        }
    }

    @Override
    public synchronized void playNext() {
        if (current != null)
            current.finish();
        else
            updateTrack(true);
        loadListenerMethod(ONSONGCHANGE, current);
    }

    @Override
    public synchronized void playPrevious() {
        Track cur = current;
        updateTrack(false);
        if (cur != null)
            cur.kill();
        startNewTrackThread();
        loadListenerMethod(ONSONGCHANGE, current);
    }

    @Override
    public synchronized void shutdown() {
        on = false;
        // Se usa kill porque con finish se cambia la cancion
        shutdownCurrent();
        this.interrupt();
        loadListenerMethod(ONSHUTDOWN, null);
    }

    @Override
    public synchronized void run() {
        PlayerHandler.setInstance(this);
        on = true;
        loadListenerMethod(ONSTARTED, null);
        loadNextTrack();
        freezePlayer();
    }

}
