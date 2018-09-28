package org.muplayer.audio;

import org.aucom.sound.Speaker;
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

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.muplayer.system.ListenersNames.*;

public class Player extends Thread implements PlayerControls {
    private volatile File rootFolder;
    private volatile Track current;

    private volatile ArrayList<String> listSoundPaths;
    private volatile ArrayList<String> listFolderPaths;
    private volatile ArrayList<PlayerListener> listListeners;

    private volatile int trackIndex;
    private volatile float currentVolume;
    private volatile boolean on;

    public static float DEFAULT_VOLUME = 85.0f;

    public enum SeekOption {
        NEXT, PREV
    }

    public Player() throws FileNotFoundException {
        this((File) null);
    }

    public Player(File rootFolder) throws FileNotFoundException {
        this.rootFolder = rootFolder;
        listSoundPaths = new ArrayList<>();
        listFolderPaths = new ArrayList<>();
        listListeners = new ArrayList<>();
        currentVolume = DEFAULT_VOLUME;
        on = false;

        if(rootFolder != null) {
            if (!rootFolder.exists())
                throw new FileNotFoundException();
            else {
                loadTracks(rootFolder);
                sortTracks();
            }
            setName("ThreadPlayer "+getId());
        }
        trackIndex = -1;
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
        File f;
        if (fldFiles != null) {

            boolean hasTracks = false;
            for (int i = 0; i < fldFiles.length; i++) {
                f = fldFiles[i];
                if (f.isDirectory())
                    loadTracks(f);
                else {
                    if (Track.isValidTrack(f.getPath())) {
                        listSoundPaths.add(f.getPath());
                        hasTracks = true;
                    }
                }
            }
            if (hasTracks)
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

    private void sortTracks() {
        listSoundPaths.sort(Comparator.naturalOrder());
        listFolderPaths.sort(Comparator.naturalOrder());
    }

    private int getFolderIndex() {
        String currentParent = current != null ? current.getDataSource().getParent() : null;
        if (currentParent != null) {
            for (int i = 0; i < listFolderPaths.size(); i++) {
                //System.out.println(listFolderPaths.get(i));
                if (listFolderPaths.get(i).equals(currentParent))
                    return i;
            }
        }
        return -1;
    }

    private Track getTrackBy(int currentIndex, SeekOption param) {
        Track next = null;
        if (param == SeekOption.NEXT) {
            if (currentIndex == getSongsCount()-1 || currentIndex < 0)
                currentIndex = 0;
            else
                currentIndex++;
            for (int i = currentIndex; i < listSoundPaths.size(); i++) {
                next = Track.getTrack(listSoundPaths.get(i));
                // Este if es por si existen archivos que no fuesen sonidos
                // en las carpetas
                if (next != null && next.isValidTrack()) {
                    trackIndex = i;
                    break;
                }
            }
            if (next != null)
                next.setGain(currentVolume);
            return next;
        }

        else {
            if (currentIndex == 0)
                currentIndex = getSongsCount()-1;
            else
                currentIndex--;

            for (int i = currentIndex; i >= 0; i--) {
                next = Track.getTrack(listSoundPaths.get(i));
                if (next != null) {
                    trackIndex = i;
                    break;
                }
            }
            if (next != null)
                next.setGain(currentVolume);
            return next;
        }
    }

    private void getNextTrack(SeekOption param) {
        current = getTrackBy(trackIndex, param);
    }

    /*private void finishTrack(Track current) {
        if (current != null && !current.isFinished())
            current.finish();
    }*/

    private String getThreadName() {
        final String trackName = current.getDataSource().getName();
        final int strLimit = trackName.length() < 10 ? trackName.length() : 10;
        return new StringBuilder()
                .append("ThreadTrack: ")
                .append(trackName, 0, strLimit).toString();
    }

    private void startThreadTrack() {
        current.setName(getThreadName());
        current.setGain(currentVolume);
        current.start();
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
        if (current != null)
            current.kill();
    }

    private void waitForSongs() {
        while (on && getSongsCount() == 0);
    }

    // Se supone que para buscar una cancion a traves de la ruta del padre
    // este ya debe haber sido validado por indexOf para saber
    // si existe o no en la ruta padre
    private Track findFirstIn(String folderPath) {
        //System.out.println("FolderToFind: "+folderPath);

        File parentFile = new File(folderPath);
        ArrayList<File> listSounds = getListSounds();

        File fileTrack = null;
        for (int i = 0; i < listSounds.size(); i++) {
            fileTrack = listSounds.get(i);
            if (fileTrack.getParentFile().equals(parentFile))
                break;
        }

        //System.out.println("FirstFound: "+fileTrack.getPath());
        if (fileTrack == null)
            return null;
        return fileTrack == null ? null :
                (Track.isValidTrack(fileTrack)?Track.getTrack(fileTrack):null);
    }

    private void startPlaying() {
        // Se debe verificar que no es un archivo de audio porque
        // cuando solo hay archivos que no son audio se lanza un
        // nullpointerexception
        waitForSongs();
        playNext();
    }

    private void waitForFinish() {
        if (current != null) {
            //Logger.getLogger(this, "Waiting for track completion").rawWarning();
            while (current.isAlive());
        }
    }

    public synchronized boolean hasSounds() {
        return !listSoundPaths.isEmpty();
    }

    public boolean existsFolder(String folderPath) {
        return listFolderPaths.parallelStream().anyMatch(fp->fp.equals(folderPath));
    }

    public boolean existsFolder(File folder) {
        return existsFolder(folder.getPath());
    }

    public boolean existsParent(String childPath) {
        return existsParent(new File(childPath));
    }

    public boolean existsParent(File child) {
        return existsFolder(child.getParent());
    }

    public byte getCurrentTrackState() {
        return current == null ? TrackStates.UNKNOWN : current.getTrackState();
    }

    public String getCurrentTrackStateToString() {
        return current == null ? "Unknown" : current.getStateToString();
    }

    public int getFoldersCount() {
        return listFolderPaths.size();
    }

    public File getRootFolder() {
        return rootFolder;
    }

    public ArrayList<String> getListFolderPaths() {
        return listFolderPaths;
    }

    public synchronized ArrayList<PlayerListener> getListListeners() {
        return listListeners;
    }

    public synchronized void analyzeFiles() {
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
    // revisar
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

    public synchronized ArrayList<File> getListSounds() {
        ArrayList<File> listFiles =
                listSoundPaths.stream()
                        .map(File::new)
                        .collect(Collectors.toCollection(ArrayList::new));
        return listFiles;
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

    public void reloadTracks() {
        listSoundPaths.clear();
        listFolderPaths.clear();
        loadTracks(rootFolder);
        sortTracks();
    }

    public synchronized Track getCurrent() {
        return current;
    }

    public synchronized Speaker getTrackSpeaker() {
        return current.getTrackLine();
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
        if (!Track.isValidTrack(sound))
            return;
        listSoundPaths.clear();
        listFolderPaths.clear();
        listSoundPaths.add(sound.getPath());
        listFolderPaths.add(sound.getParent());
        if (isPlaying())
            current.finish();
        else if (isAlive()) {
            current = Track.getTrack(sound);
            startThreadTrack();
        }
        else
            start();
    }

    @Override
    public synchronized void open(List<File> listSounds) {
        if (!listSounds.parallelStream().anyMatch(fileSound->Track.isValidTrack(fileSound)))
            return;

        listSoundPaths.clear();
        listFolderPaths.clear();
        loadTracks(listSounds);
        sortTracks();
    }

    @Override
    public synchronized void addMusic(List<File> listSounds) {
        if (!listSounds.isEmpty()) {
            final Consumer<File> consumer = sound->{
                if (sound.isDirectory())
                    loadTracks(sound);
                else if (Track.isValidTrack(sound))
                    listSoundPaths.add(sound.getPath());
            };
            listSounds.stream().forEach(consumer);
            /*if (hasSounds()) {
                suspend();
                sortTracks();
                resume();
            }*/
        }
    }

    @Override
    public synchronized void addMusic(File musicFolder) {
        if (musicFolder.isDirectory()) {
            if (rootFolder == null)
                rootFolder = musicFolder;
            boolean validSort = !hasSounds();
            loadTracks(musicFolder);
            if (validSort)
                sortTracks();
            /*if (hasSounds()) {
                suspend();
                sortTracks();
                resume();
            }*/
        }
        else if (Track.isValidTrack(musicFolder)) {
            listSoundPaths.add(musicFolder.getPath());
            final String parentPath = musicFolder.getParent();
            if (!listFolderPaths.parallelStream().anyMatch(sp->parentPath.equals(sp)))
                listFolderPaths.add(parentPath);
        }
    }

    public synchronized void printTracks() {
        Logger.getLogger(this, "------------------------------").rawInfo();
        if (rootFolder == null)
            Logger.getLogger(this, "Music in folder").rawInfo();
        else
            Logger.getLogger(this, "Music in folder "+rootFolder.getName()).rawInfo();
        Logger.getLogger(this, "------------------------------").rawInfo();

        if (rootFolder != null) {
            File fileTrack;
            for (int i = 0; i < getSongsCount(); i++) {
                fileTrack = new File(listSoundPaths.get(i));
                if (current != null && fileTrack.getPath().equals(current.getDataSource().getPath()))
                    Logger.getLogger(this, "Track "+(i+1)+": "
                            +fileTrack.getName()).rawWarning();
                else
                    Logger.getLogger(this, "Track "+(i+1)+": "
                            +fileTrack.getName()).rawInfo();
            }
            Logger.getLogger(this, "------------------------------").rawInfo();
        }
    }

    public synchronized void printFolderTracks() {
        File parentFolder = current == null ? null : current.getDataSource().getParentFile();
        Logger.getLogger(this, "------------------------------").rawInfo();

        if (parentFolder == null)
            Logger.getLogger(this, "Music in current folder").rawInfo();
        else
            Logger.getLogger(this, "Music in folder "+parentFolder.getName()).rawInfo();
        Logger.getLogger(this, "------------------------------").rawInfo();

        if (parentFolder != null) {
            File fileTrack;
            File currentFile = current.getDataSource();

            for (int i = 0; i < getSongsCount(); i++) {
                fileTrack = new File(listSoundPaths.get(i));
                if (fileTrack.getParentFile().equals(parentFolder)) {
                    if (fileTrack.getPath().equals(currentFile.getPath()))
                        Logger.getLogger(this, "Track "+(i+1)+": "
                                +fileTrack.getName()).rawWarning();
                    else
                        Logger.getLogger(this, "Track "+(i+1)+": "
                                +fileTrack.getName()).rawInfo();
                }
            }
            Logger.getLogger(this, "------------------------------").rawInfo();
        }
    }

    public synchronized void printFolders() {
        Logger.getLogger(this, "------------------------------").rawInfo();
        if (rootFolder == null)
            Logger.getLogger(this, "Folders").rawInfo();
        else
            Logger.getLogger(this, "Folders in "+rootFolder.getName()).rawInfo();
        Logger.getLogger(this, "------------------------------").rawInfo();
        for (int i = 0; i < getFoldersCount(); i++)
            Logger.getLogger(this, "Folder "+(i+1)+": "
                    +new File(listFolderPaths.get(i)).getName()).rawInfo();
        Logger.getLogger(this, "------------------------------").rawInfo();
    }

    public synchronized int getSongsCount() {
        return listSoundPaths.size();
    }

    public synchronized void seekFolder(SeekOption param) {
        seekFolder(param, 1);
    }

    public synchronized void seekFolder(SeekOption option, int jumps) {
        int folderIndex = getFolderIndex();
        //Logger.getLogger(this, "Current FolderIndex en seekFolder: "+ folderIndex).rawInfo();
        //Logger.getLogger(this, "Jumps: "+ jumps).rawInfo();
        //Logger.getLogger(this, "Current Parent en seekFolder: "+ current.getDataSource().getParent()).rawInfo();
        if (folderIndex != -1) {
            int newFolderIndex;
            String parentToFind;
            Track next;

            if (option == SeekOption.NEXT) {
                newFolderIndex = folderIndex+jumps;
                if (newFolderIndex >= getFoldersCount())
                    parentToFind = listFolderPaths.get(0);
                else
                    parentToFind = listFolderPaths.get(newFolderIndex);
            }
            else {
                newFolderIndex = folderIndex - jumps;
                //Logger.getLogger(this, "SeekPrevFolder", "NewFolderIndex: "+newFolderIndex)
                //        .info();
                if (newFolderIndex < 0)
                    parentToFind = listFolderPaths.get(listFolderPaths.size()-1);
                else
                    parentToFind  = listFolderPaths.get(newFolderIndex);
            }

            next = findFirstIn(parentToFind);
            //System.out.println("NextSeek: "+(next==null?"Null":next.getTitle()));
            if (next != null) {
                try {
                    trackIndex = listSoundPaths.indexOf(next.getDataSource().getCanonicalPath());
                    if (current != null)
                        current.kill();
                    current = next;
                    startThreadTrack();
                } catch (IOException e) {
                    e.printStackTrace();
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

    public void play(int index) {
        if (index < 0 || index >= getSongsCount())
            return;
        if (current != null) {
            current.kill();
            String soundPath = listSoundPaths.get(index);
            Track track = Track.getTrack(soundPath);
            current = track;
            startThreadTrack();
        }
    }

    // Reproduce archivo de audio en la lista
    // (is alive)
    @Override
    public synchronized void play(File track) {
        System.out.println("xd");
        int indexOf = listSoundPaths.indexOf(track.getPath());
        if (indexOf == -1) {
            if (Track.isValidTrack(track)) {
                listSoundPaths.add(track.getPath());
                if (!existsFolder(track.getParent()))
                    listFolderPaths.add(track.getParent());
            }
        }
        else {
            trackIndex = indexOf;
            if (current != null)
                current.kill();
            //Logger.getLogger(this, "SelectedTrack: "+track.getName()).rawWarning();
            current = Track.getTrack(track);
            //Logger.getLogger(this, "Current: "+current.getTitle()).rawWarning();
            startThreadTrack();
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
            startThreadTrack();
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
    public synchronized void stopTrack()
            throws UnsupportedAudioFileException, IOException, LineUnavailableException {
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
    public synchronized void seek(double seconds) {
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

    public double getProgress() {
        return current == null ? 0 : current.getProgress();
    }

    @Override
    public synchronized void playNext() {
        if (current != null)
            current.kill();
        getNextTrack(SeekOption.NEXT);
        waitForFinish();
        startThreadTrack();
        loadListenerMethod(ONSONGCHANGE, current);
    }

    @Override
    public synchronized void playPrevious() {
        if (current != null)
            current.kill();
        getNextTrack(SeekOption.PREV);
        waitForFinish();
        startThreadTrack();
        loadListenerMethod(ONSONGCHANGE, current);
    }

    public void playFolder(String path) {
        if (current != null)
            current.kill();

        int fldIndex = listFolderPaths.indexOf(path);
        if (fldIndex != -1) {

        }
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
        startPlaying();
        freezePlayer();
    }

}
