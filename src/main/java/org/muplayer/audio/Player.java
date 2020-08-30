package org.muplayer.audio;

import org.aucom.sound.Speaker;
import org.muplayer.audio.info.AudioTag;
import org.muplayer.audio.info.SongData;
import org.muplayer.audio.interfaces.PlayerControls;
import org.muplayer.audio.interfaces.PlayerListener;
import org.muplayer.audio.model.Album;
import org.muplayer.audio.model.Artist;
import org.muplayer.audio.model.SeekOption;
import org.muplayer.audio.model.TrackInfo;
import org.muplayer.audio.trackstates.TrackState;
import org.muplayer.audio.trackstates.UnknownState;
import org.muplayer.audio.util.PlayerInfo;
import org.muplayer.system.AudioUtil;
import org.muplayer.system.LineUtil;
import org.muplayer.thread.ListenerRunner;
import org.muplayer.thread.TaskRunner;
import org.muplayer.thread.ThreadManager;
import org.orangelogger.sys.Logger;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.muplayer.system.ListenersNames.*;

public class Player extends Thread implements PlayerControls {
    private volatile File rootFolder;
    private volatile Track current;

    private final List<String> listSoundPaths;
    private final List<String> listFolderPaths;
    private final List<PlayerListener> listListeners;

    private volatile int trackIndex;
    private volatile float currentVolume;
    private volatile boolean on;
    private volatile boolean isMute;

    public static final float DEFAULT_VOLUME = AudioUtil.convertLineRangeToVolRange(AudioUtil.MiDDLE_VOL);

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
        isMute = false;

        checkRootFolder();
        setName("ThreadPlayer "+getId());
        trackIndex = -1;
        disableLogging();
    }

    private void checkRootFolder() throws FileNotFoundException {
        if(rootFolder != null) {
            if (!rootFolder.exists())
                throw new FileNotFoundException(rootFolder.getPath());
            else {
                loadTracks(rootFolder);
                sortTracks();
            }
        }
    }

    public Player(String folderPath) throws FileNotFoundException {
        this(new File(folderPath));
    }

    private void disableLogging() {
        java.util.logging.Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
    }

    // Problema con ogg al leer tagInfo archivo

    // no se revisaran si los archivos son sonidos por
    // ahora porque se piensa en reducir tiempos de carga
    // haciendo de la revision en tiempo de ejecucion

    private void loadTracks(File folder) {
        File[] fldFiles = folder.listFiles();
        File f;
        if (fldFiles != null) {
            // se analiza carpeta y se agregan sonidos recursivamente
            boolean hasTracks = false;
            String filePath;

            for (int i = 0; i < fldFiles.length; i++) {
                f = fldFiles[i];
                if (f.isDirectory())
                    loadTracks(f);
                else {
                    try {
                        filePath = f.getCanonicalPath();
                        if (Track.isValidTrack(filePath)) {
                            listSoundPaths.add(filePath);
                            hasTracks = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // si la carpeta tiene sonidos se agrega a la lista de carpetas
            if (hasTracks)
                try {
                    listFolderPaths.add(folder.getCanonicalPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void loadTracks(List<File> listFiles) {
        listFiles.forEach(f->{
            if (f.isDirectory())
                loadTracks(f);
            else
                listSoundPaths.add(f.getPath());
        });
    }

    private void sortTracks() {
        final Comparator<String> comparator = Comparator.naturalOrder();
        listSoundPaths.sort(comparator);
        listFolderPaths.sort(comparator);
    }

    private int getFolderIndex() {
        final String currentParent = current != null ? current.getDataSource().getParent() : null;
        return currentParent != null ? listFolderPaths.indexOf(currentParent) : -1;
    }

    private Track getTrackBy(int currentIndex, SeekOption param) {
        Track next = null;
        if (param == SeekOption.NEXT) {
            if (currentIndex == getSongsCount()-1 || currentIndex < 0)
                currentIndex = 0;
            else
                currentIndex++;
            for (int i = currentIndex; i < listSoundPaths.size(); i++) {
                next = Track.getTrack(listSoundPaths.get(i), this);
                // Este if es por si existen archivos que no fuesen sonidos
                // en las carpetas
                if (next != null && next.isValidTrack()) {
                    trackIndex = i;
                    break;
                }
            }
            return next;
        }

        else {
            if (currentIndex == 0)
                currentIndex = getSongsCount()-1;
            else
                currentIndex--;

            for (int i = currentIndex; i >= 0; i--) {
                next = Track.getTrack(listSoundPaths.get(i), this);
                if (next != null) {
                    trackIndex = i;
                    break;
                }
            }
            return next;
        }
    }

    private void getNextTrack(SeekOption param) {
        current = getTrackBy(trackIndex, param);
    }

    private String getThreadName() {
        final String trackName = current.getDataSource().getName();
        final int strLimit = Math.min(trackName.length(), 10);
        return new StringBuilder("ThreadTrack: ")
                .append(trackName, 0, strLimit).toString();
    }

    // ojo aqui con los errores que puedan suceder
    private void startThreadTrack() {
        if (current != null) {
            current.setName(getThreadName());
            current.setGain(isMute ? 0 : currentVolume);
            current.start();
        }
    }

    public void loadListenerMethod(String methodName, Track track) {
        if (!listListeners.isEmpty())
            TaskRunner.execute(new ListenerRunner(listListeners, methodName, track));
    }

    private synchronized void freezePlayer() {
        ThreadManager.freezeThread(this);
    }

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
        final File parentFile = new File(folderPath);
        final List<File> listSounds = getListSounds();
        final File fileTrack = listSounds.stream()
                .filter(file -> file.getParentFile().equals(parentFile))
                .findFirst().orElse(null);
        return fileTrack != null && Track.isValidTrack(fileTrack)
                ? Track.getTrack(fileTrack, this)
                : null;
    }

    private int moveToFolder(String folderPath) {
        File parentFile = new File(folderPath);
        List<File> listSounds = getListSounds();

        File fileTrack;
        int trackIndex = -1;

        for (int i = 0; i < listSounds.size(); i++) {
            fileTrack = listSounds.get(i);
            if (fileTrack.getParentFile().equals(parentFile)) {
                trackIndex = i;
                break;
            }
        }
        return trackIndex-1;

    }

    private void startPlaying() {
        waitForSongs();
        playNext();
    }

    private void muteCurrent() {
        if (current != null) {
            current.mute();
        }
    }

    private void playFolderSongs(String fldPath) {
        for (int i = 0; i < listSoundPaths.size(); i++) {
            if (new File(listSoundPaths.get(i))
                    .getParent().equals(fldPath)) {
                play(i);
                break;
            }
        }
    }

    public boolean hasSounds() {
        return !listSoundPaths.isEmpty();
    }

    public boolean isActive() {
        return isAlive() && hasSounds();
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

    public TrackState getCurrentTrackState() {
        return current == null ? new UnknownState(current) : current.getTrackState();
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

    public List<String> getListFolderPaths() {
        return listFolderPaths;
    }

    // Test
    // revisar
    public synchronized void jumpTrack(int jumps, SeekOption option) {
        if (option == SeekOption.NEXT) {
            trackIndex+=jumps;
            if (trackIndex >= listSoundPaths.size())
                trackIndex = 0;
        }
        else {
            trackIndex -= jumps;
            if (trackIndex < 0)
                trackIndex = listSoundPaths.size()-1;
        }
        play(trackIndex);
    }


    public synchronized List<String> getListSoundPaths() {
        return listSoundPaths;
    }

    public synchronized List<File> getListSounds() {
        return listSoundPaths.stream()
                        .map(File::new)
                        .collect(Collectors.toList());
    }

    // Se supone que todos los tracks serian validos
    // sino rescatar de los que sean no mas
    public synchronized List<AudioTag> getTrackTags() {
        final List<AudioTag> listTags = new ArrayList<>();

        // probar con map cunado se retornan nulls
        listSoundPaths.forEach(soundPath -> {
            try {
                AudioTag tag = new AudioTag(soundPath);
                if (tag.isValidFile())
                    listTags.add(tag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return listTags;
    }

    public synchronized List<TrackInfo> getTracksInfo() {
        final List<TrackInfo> listInfo = new ArrayList<>();
        listSoundPaths.forEach(soundPath->{
            try {
                AudioTag tag = new AudioTag(soundPath);
                if (tag.isValidFile())
                    listInfo.add(new SongData(tag));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return listInfo;
    }

    public synchronized List<Artist> getArtists() {
        final List<TrackInfo> listTracks = getTracksInfo();
        final List<Artist> listArtists = new ArrayList<>();

        listTracks.parallelStream()
                .forEach(track->{
                    String art = track.getArtist();
                    if (art == null)
                        art = "Unknown";
                    synchronized (listArtists) {
                        String finalArt = art;
                        Artist artist = listArtists.parallelStream()
                                .filter(a->a.getName().equalsIgnoreCase(finalArt)).findFirst().orElse(null);

                        if (artist == null) {
                            artist = new Artist();
                            artist.setName(finalArt);
                            listArtists.add(artist);
                        }
                        artist.addTrack(track);
                    }
                });
        listArtists.sort(Comparator.comparing(Artist::getName));
        return listArtists;
    }

    public synchronized List<Album> getAlbums() {
        List<TrackInfo> listTracks = getTracksInfo();
        List<Album> listAlbums = new ArrayList<>();

        listTracks.parallelStream()
                .forEach(track->{
                    String alb = track.getAlbum();
                    if (alb == null)
                        alb = "Unknown";
                    synchronized (listAlbums) {
                        String finalAlb = alb;
                        Album album = listAlbums.parallelStream()
                                .filter(a->a.getName().equalsIgnoreCase(finalAlb)).findFirst().orElse(null);

                        if (album == null) {
                            album = new Album();
                            album.setName(finalAlb);
                            listAlbums.add(album);
                        }
                        album.addTrack(track);
                    }
                });
        listAlbums.sort(Comparator.comparing(Album::getName));
        return listAlbums;
    }

    public synchronized void addPlayerListener(PlayerListener listener) {
        listListeners.add(listener);
    }

    public synchronized List<PlayerListener> getListeners() {
        return listListeners;
    }

    public synchronized void removePlayerListener(PlayerListener reference) {
        listListeners.removeIf(listener->listener.equals(reference));
    }

    public synchronized void removeAllListeners() {
        listListeners.clear();
    }

    public synchronized void reloadTracks() {
        listSoundPaths.clear();

        if (rootFolder != null) {
            int currentIndex = trackIndex;

            listSoundPaths.clear();
            listFolderPaths.clear();

            loadTracks(rootFolder);
            sortTracks();

            int songCount = getSongsCount();
            if (songCount > currentIndex) {
                trackIndex = currentIndex;
            }
        }
    }

    public PlayerInfo getInfo() {
        return new PlayerInfo(this);
    }

    public synchronized Track getCurrent() {
        return current;
    }

    public synchronized TrackInfo getNext() {
        int songsCount = getSongsCount();
        int nextIndex = trackIndex == -1 ? 0 : (trackIndex == songsCount-1 ? 0 : trackIndex+1);
        return Track.getTrack(listSoundPaths.get(nextIndex), this);
    }

    public synchronized TrackInfo getPrevious() {
        int songsCount = getSongsCount();
        int prevIndex = trackIndex == -1 ? 0 : (trackIndex == 0 ? songsCount-1 : trackIndex-1);
        return Track.getTrack(listSoundPaths.get(prevIndex), this);
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
        return current != null && current.isPlaying();
    }

    @Override
    public synchronized boolean isPaused() {
        return current != null && current.isPaused();
    }

    @Override
    public synchronized boolean isStopped() {
        return current != null && current.isStopped();
    }

    @Override
    public synchronized boolean isFinished() {
        return current != null && current.isFinished();
    }

    @Override
    public synchronized boolean isMute() {
        //return current != null && current.isMute();
        return isMute;
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
            current = Track.getTrack(sound, this);
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
            listSounds.forEach(consumer);
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

        if (getCurrent() == null)
            return;

        File currentTrackFile = getCurrent().getDataSource();
        File folder;

        for (int i = 0; i < getFoldersCount(); i++) {
            folder = new File(listFolderPaths.get(i));
            if (folder.getPath().equals(currentTrackFile.getParentFile().getPath())) {
                Logger.getLogger(this, "Folder "+(i+1)+": "
                        +folder.getName()).rawWarning();
            }
            else {
                Logger.getLogger(this, "Folder "+(i+1)+": "
                        +folder.getName()).rawInfo();
            }

        }
        Logger.getLogger(this, "------------------------------").rawInfo();
    }

    public int getSongsCount() {
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
            final String soundPath = listSoundPaths.get(index);
            final Track track = Track.getTrack(soundPath, this);
            if (track != null) {
                current = track;
                startThreadTrack();
                trackIndex = index;
                loadListenerMethod(ONPLAYED, current);
            }
        }
    }

    // Reproduce archivo de audio en la lista
    // (is alive)
    @Override
    public synchronized void play(File track) {
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
            current = Track.getTrack(track, this);
            startThreadTrack();
            loadListenerMethod(ONPLAYED, current);
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
            current = Track.getTrack(song, this);
            startThreadTrack();
            loadListenerMethod(ONPLAYED, current);
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
    public synchronized void gotoSecond(double second) {
        if (current != null) {
            try {
                current.gotoSecond(second);
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized float getGain() {
        return current == null ? currentVolume : current.getGain();
    }

    // 0-100
    @Override
    public synchronized void setGain(float volume) {
        currentVolume = volume;
        if (current != null)
            current.setGain(volume);
        isMute = currentVolume == 0;
    }

    @Override
    public float getSystemVolume() {
        return LineUtil.getFormattedMasterVolume();
    }

    @Override
    public void setSystemVolume(float volume) {
        LineUtil.setFormattedMasterVolume(volume);
    }

    @Override
    public synchronized void mute() {
        isMute = true;
        if (current != null)
            current.mute();
    }

    @Override
    public synchronized void unmute() {
        isMute = false;
        if (current != null) {
            current.setGain(currentVolume);
        }
        else
            currentVolume = 100;
    }

    public double getProgress() {
        return current == null ? 0 : current.getProgress();
    }

    public String getFormattedProgress() {
        return current == null ? "00:00" : current.getFormattedProgress();
    }

    @Override
    public synchronized void playNext() {
        if (current != null)
            current.kill();
        getNextTrack(SeekOption.NEXT);
        muteCurrent();
        startThreadTrack();
        loadListenerMethod(ONSONGCHANGE, current);
    }

    @Override
    public synchronized void playPrevious() {
        if (current != null)
            current.kill();
        getNextTrack(SeekOption.PREV);
        muteCurrent();
        startThreadTrack();
        loadListenerMethod(ONSONGCHANGE, current);
    }

    public void playFolder(String path) {
        if (current != null)
            current.kill();

        int fldIndex = listFolderPaths.indexOf(path);
        if (fldIndex != -1)
            playFolderSongs(path);
    }

    /*public void playFolder(int index) {
        if (index > -1 && index > listFolderPaths.size())
            playFolderSongs(listFolderPaths.get(index));
    }*/

    public void playFolder(int index) {
        if (index < getFoldersCount()) {
            trackIndex = moveToFolder(listFolderPaths.get(index));
            playNext();
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
        loadListenerMethod(ONSTARTED, null);
        on = true;
        startPlaying();
        freezePlayer();
    }

}
