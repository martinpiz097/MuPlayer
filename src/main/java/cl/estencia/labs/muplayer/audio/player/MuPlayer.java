package cl.estencia.labs.muplayer.audio.player;

import cl.estencia.labs.aucom.core.util.AudioSystemManager;
import cl.estencia.labs.ebot.bus.MessageBus;
import cl.estencia.labs.ebot.bus.exception.BusException;
import cl.estencia.labs.ebot.utils.threads.Interruptor;
import cl.estencia.labs.muplayer.audio.interfaces.SystemVolumeController;
import cl.estencia.labs.muplayer.audio.model.Album;
import cl.estencia.labs.muplayer.audio.model.Artist;
import cl.estencia.labs.muplayer.audio.model.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.model.TrackIndexed;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.state.TrackStateName;
import cl.estencia.labs.muplayer.core.bus.util.MessageBusUtil;

import cl.estencia.labs.muplayer.core.bus.listener.PlayerResponseListener;
import cl.estencia.labs.muplayer.core.bus.message.Messages;
import cl.estencia.labs.muplayer.core.bus.model.MuPlayerResponse;
import cl.estencia.labs.muplayer.core.bus.model.SkipData;
import cl.estencia.labs.muplayer.audio.common.enums.SeekOption;
import cl.estencia.labs.muplayer.audio.util.AudioFileUtil;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import cl.estencia.labs.muplayer.core.util.FilterUtil;
import cl.estencia.labs.muplayer.audio.util.MuPlayerUtil;
import cl.estencia.labs.muplayer.io.file.AudioFileScanner;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cl.estencia.labs.muplayer.core.bus.message.MuPlayerTopic.*;
import static cl.estencia.labs.muplayer.audio.common.enums.SeekOption.NEXT;
import static cl.estencia.labs.muplayer.audio.common.enums.SeekOption.PREV;

@Slf4j
public class MuPlayer extends Player implements SystemVolumeController {
    private final File rootFolder;
    private final AtomicReference<Track> currentTrack;

    private final List<Track> listTracks;
    private final List<AudioFileScanner> listAudioFileScanners;
    private final List<File> listFolders;

    private final PlayerStatusData playerStatusData;
    @Getter private final MuPlayerUtil muPlayerUtil;
    private final AudioSystemManager audioSystemManager;

    private final Interruptor interruptor;
    private final MessageBus messageBus;

    public MuPlayer() throws FileNotFoundException {
        this((File) null);
    }

    public MuPlayer(File rootFolder) throws FileNotFoundException {
        this.rootFolder = rootFolder;
        this.currentTrack = new AtomicReference<>();
        this.listTracks = CollectionUtil.newFastArrayList();
        this.listAudioFileScanners = CollectionUtil.newFastArrayList();
        this.listFolders = CollectionUtil.newMinimalFastArrayList();
        this.playerStatusData = new PlayerStatusData();
        this.muPlayerUtil = new MuPlayerUtil(this, playerStatusData);
        this.audioSystemManager = new AudioSystemManager();
        this.interruptor = Interruptor.manual(this);
        this.messageBus = MessageBusUtil.getMessageBus();

        setName("MuPlayer " + getId());
        configureEventListeners();
    }

    public MuPlayer(String folderPath) throws FileNotFoundException {
        this(new File(folderPath));
    }

    private void killActiveTracks() {
        listTracks.parallelStream()
                .filter(Track::isActive)
                .forEach(Track::kill);
    }

    private void loadTracks(File folderToLoad) {
//        Files.find()

        try (Stream<Path> folderPaths = Files.walk(
                Path.of(folderToLoad.toURI())).parallel()) {
            if (hasSounds()) {
                killActiveTracks();

                listTracks.clear();
                listFolders.clear();
                listAudioFileScanners.clear();
            }

            folderPaths
                    .filter(AudioFileUtil::hasAudioFormatExtension)
                    .map(path -> muPlayerUtil.
                            loadTrackFromFile(path.toFile()))
                    .filter(Objects::nonNull)
                    .sorted(MuPlayerUtil.TRACKS_SORT_COMPARATOR)
                    .forEachOrdered(listTracks::add);

            listTracks.parallelStream()
                    .map(track -> track.getDataSource().getParentFile())
                    .distinct()
                    .sorted(MuPlayerUtil.FOLDERS_COMPARATOR)
                    .forEachOrdered(listFolders::add);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void playFolderSongs(String fldPath) {
        final Predicate<Track> filter = FilterUtil.getPlayFolderFilter(fldPath);
        final TrackIndexed trackIndexed = muPlayerUtil.getTrackIndexedFromCondition(filter);

        if (trackIndexed != null) {
            play(trackIndexed.getIndex());
        }
    }

    private void configureEventListeners() {
        try {
            messageBus.subscribe(START.name(), message -> {
                if (!isAlive()) {
                    start();
                }

                loadTracks(rootFolder);
                playNext();
                messageBus.publish(Messages.playerResponse(currentTrack, playerStatusData));
            });

            messageBus.subscribe(RELOAD.name(), message -> {
                reload();
                messageBus.publish(Messages.playerResponse(currentTrack, playerStatusData));
            });

            messageBus.subscribe(PLAY_NEXT.name(), message -> {
                playNext();
                messageBus.publish(Messages.playerResponse(currentTrack, playerStatusData));
            });

            messageBus.subscribe(PLAY_PREVIOUS.name(), message -> {
                playPrevious();
                messageBus.publish(Messages.playerResponse(currentTrack, playerStatusData));
            });

            messageBus.subscribe(PLAY_INDEX.name(), message -> {
                int index = message.getData(Integer.class);
                play(index);
                messageBus.publish(Messages.playerResponse(currentTrack, playerStatusData));
            });

            messageBus.subscribe(PLAY.name(), message -> {
                play();
                messageBus.publish(Messages.playerResponse(currentTrack, playerStatusData));
            });

            messageBus.subscribe(SHUTDOWN.name(), message -> {
                shutdown();
                messageBus.shutdown();
            });

            messageBus.subscribe(PAUSE.name(), message -> {
                pause();
            });

            messageBus.subscribe(RESUME.name(), message -> {
                resumeTrack();
            });

            messageBus.subscribe(STOP.name(), message -> {
                stopTrack();
            });

            messageBus.subscribe(SKIP_TRACKS.name(), message -> {
                SkipData skipData = message.getData(SkipData.class);
                skipTracks(skipData.getSkipCount(), skipData.getSeekOption());

                messageBus.publish(Messages.playerResponse(currentTrack, playerStatusData));
            });

            messageBus.subscribe(SEEK_FOLDER.name(), message -> {
                SkipData skipData = message.getData(SkipData.class);
                seekFolder(skipData.getSeekOption(), skipData.getSkipCount());

                messageBus.publish(Messages.playerResponse(currentTrack, playerStatusData));
            });

            messageBus.subscribe(MUTE.name(), message -> {
                mute();
            });

            messageBus.subscribe(UNMUTE.name(), message -> {
                unMute();
            });
            
            messageBus.subscribe(GET_VOLUME.name(), message -> {
            });
            
            messageBus.subscribe(SET_VOLUME.name(), message -> {
                float volume = message.getData(Float.class);
                setVolume(volume);
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public TrackStateName getCurrentTrackState() {
        return currentTrack.get() != null
                ? currentTrack.get().getStateName() :
                TrackStateName.UNKNOWN;
    }

    @Override
    public PlayerStatusData getPlayerStatusData() {
        return playerStatusData;
    }

    @Override
    public int getFoldersCount() {
        return listFolders.size();
    }

    @Override
    public File getRootFolder() {
        return rootFolder;
    }

    @Override
    public List<File> getListFolders() {
        return listFolders;
    }

    @Override
    public synchronized void skipTracks(int skipCount, SeekOption option) {
        int newIndex;
        if (option == NEXT) {
            newIndex = playerStatusData.getCurrentTrackIndex() + skipCount;
            if (newIndex >= listTracks.size()) {
                newIndex = 0;
            }
        } else {
            newIndex = playerStatusData.getCurrentTrackIndex() - skipCount;
            if (newIndex < 0) {
                newIndex = listTracks.size() - 1;
            }
        }

        play(newIndex);
    }

    @Override
    public synchronized List<File> getListSoundFiles() {
        return listTracks.stream()
                .map(Track::getDataSource)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(CollectionUtil::newLinkedList));
    }

    @Override
    public synchronized List<Track> getTracks() {
        return listTracks;
    }

    @Override
    public synchronized List<Artist> getArtists() {
        final List<Track> trackList = getTracks();
        final Set<Artist> setArtists = new HashSet<>(trackList.size() + 1);

        trackList.parallelStream()
                .forEach(track -> {
                    final String artistName = track.getArtist() != null ? track.getArtist() : "Unknown";
                    synchronized (setArtists) {
                        Artist artist = setArtists.parallelStream()
                                .filter(art -> art.getName().equals(artistName))
                                .findFirst().orElse(null);
                        if (artist != null) {
                            artist.addTrack(track);
                        } else {
                            artist = new Artist(artistName);
                            artist.addTrack(track);
                            setArtists.add(artist);
                        }
                    }
                });

        final List<Artist> listArtists = CollectionUtil.newFastList(setArtists);
        listArtists.sort(Comparator.comparing(Artist::getName));
        return listArtists;
    }

    @Override
    public synchronized List<Album> getAlbums() {
        final List<Track> listTracks = getTracks();
        final Set<Album> setAlbums = new HashSet<>(listTracks.size() + 1);

        listTracks.parallelStream()
                .forEach(track -> {
                    final String albumName = track.getAlbum() != null ? track.getAlbum() : "Unknown";
                    synchronized (setAlbums) {
                        Album album = setAlbums.parallelStream()
                                .filter(alb -> alb.getName().equals(albumName))
                                .findFirst().orElse(null);
                        if (album != null) {
                            album.addTrack(track);
                        } else {
                            album = new Album(albumName);
                            album.addTrack(track);
                            setAlbums.add(album);
                        }
                    }
                });

        final List<Album> listAlbums = CollectionUtil.newFastList(setAlbums);
        listAlbums.sort(Comparator.comparing(Album::getName));
        return listAlbums;
    }

    @Override
    public void addResponseListener(PlayerResponseListener responseListener) {
        try {
            messageBus.subscribe(PLAYER_RESPONSE.name(), message -> {
               responseListener.onPlayerResponse(message.getData(MuPlayerResponse.class));
            });
        } catch (BusException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void removeAllResponseListeners() {
        try {
            messageBus.unsubscribeAll(PLAYER_RESPONSE.name());
        } catch (BusException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public AtomicReference<Track> getCurrentTrack() {
        return currentTrack;
    }

    @Override
    public synchronized Track getNext() {
        return muPlayerUtil.getTrackBySeekOption(NEXT);
    }

    @Override
    public synchronized Track getPrevious() {
        return muPlayerUtil.getTrackBySeekOption(PREV);
    }

    @Override
    public int getSongsCount() {
        return listTracks.size();
    }

    @Override
    public boolean hasSounds() {
        return !listTracks.isEmpty();
    }

    @Override
    public synchronized boolean isOn() {
        return playerStatusData.isOn();
    }

    @Override
    public synchronized boolean isPlaying() {
        return currentTrack.get() != null && currentTrack.get().isPlaying();
    }

    @Override
    public synchronized boolean isPaused() {
        return currentTrack.get() != null && currentTrack.get().isPaused();
    }

    @Override
    public synchronized boolean isStopped() {
        return currentTrack.get() != null && currentTrack.get().isStopped();
    }

    @Override
    public synchronized boolean isMute() {
        return playerStatusData.isMute();
    }

    // ojo cuando se agrega musica de carpetas que estan fuera de rootFolder
    // puede ocasionar problemas
    @Override
    public synchronized void addMusic(Collection<File> soundCollection) {
//        if (!soundCollection.isEmpty()) {
//            soundCollection.forEach(this::setupTracksList);
//            muPlayerUtil.waitForTracksLoading();
//        }
    }

    @Override
    public synchronized void addMusic(File folderOrFile) {
//        if (folderOrFile.isDirectory()) {
//            if (rootFolder == null) {
//                rootFolder = folderOrFile;
//            }
//            final boolean validSort = !hasSounds();
//            setupTracksList(folderOrFile);
//            muPlayerUtil.waitForTracksLoading();
//            if (validSort) {
//                muPlayerUtil.sortTracks();
//            }
//        } else if (audioSupportUtil.isSupportedFile(folderOrFile)) {
//            final Track track = trackBuilder.getTrack(folderOrFile, this);
//            if (track != null) {
//                listTracks.add(track);
//                final File parent = folderOrFile.getParentFile();
//                if (listFolders.parallelStream().noneMatch(parent::equals)) {
//                    listFolders.add(parent);
//                }
//            }
//        }
    }

    @Override
    public synchronized void seekFolder(SeekOption option) {
        seekFolder(option, 1);
    }

    @Override
    public synchronized void seekFolder(SeekOption option, int jumps) {
        final int folderIndex = muPlayerUtil.getFolderIndex(currentTrack.get());
        if (folderIndex != -1) {
            final int newFolderIndex;
            final File parentToFind;

            if (option == NEXT) {
                newFolderIndex = folderIndex + jumps;
                parentToFind = listFolders.get(newFolderIndex >= getFoldersCount()
                        ? 0 : newFolderIndex);
            } else {
                newFolderIndex = folderIndex - jumps;
                parentToFind = listFolders.get(newFolderIndex < 0
                        ? listFolders.size() - 1 : newFolderIndex);
            }

            final TrackIndexed firstIn = muPlayerUtil.findFirstIn(parentToFind.getPath());
            play(firstIn.getIndex());
        }
    }

    @Override
    public synchronized void play() {
        if (!isAlive()) {
            start();
        } else if (currentTrack.get() != null) {
            currentTrack.get().play();
        }
    }

    @Override
    public void play(int index) {
        if (index < 0 || index >= getSongsCount()) {
            return;
        }

        muPlayerUtil.playNewTrack(index);
    }

    @Override
    public synchronized void playNext() {
        int nextIndex = AudioFileUtil.getIndexFromOption(NEXT, playerStatusData, getSongsCount());
        play(nextIndex);
    }

    @Override
    public synchronized void playPrevious() {
        int prevIndex = AudioFileUtil.getIndexFromOption(PREV, playerStatusData, getSongsCount());
        play(prevIndex);
    }

    @Override
    public void playFolder(String path) {
        playFolderSongs(path);
    }

    @Override
    public void playFolder(int folderIndex) {
        final int foldersCount = getFoldersCount();
        if (folderIndex >= foldersCount) {
            folderIndex = foldersCount - 1;
        }

        final int newIndex = muPlayerUtil.seekToFolder(listFolders.get(folderIndex).getPath());
        play(newIndex);
    }

    // Reproduce archivo de audio en la lista
    // (is alive)
    // TODO REVISAR
    @Override
    public synchronized void play(File trackFile) {
        Predicate<Track> filter = FilterUtil.getTrackFilterByPath(trackFile.getPath());
        TrackIndexed trackIndexed = muPlayerUtil.getTrackIndexedFromCondition(filter);

        if (trackIndexed == null) {
            Track newTrack = muPlayerUtil.loadTrackFromFile(trackFile);
            listTracks.add(newTrack);
            if (!CollectionUtil.existsFolder(listFolders, trackFile.getParent())) {
                listFolders.add(trackFile.getParentFile());
            }

            trackIndexed = new TrackIndexed(newTrack, getSongsCount());
        }

        play(trackIndexed.getIndex());
    }

    @Override
    public synchronized void play(String trackName) {
        Predicate<Track> filter = FilterUtil.getTrackFilterByName(trackName);
        TrackIndexed trackIndexed = muPlayerUtil.getTrackIndexedFromCondition(filter);

        if (trackIndexed != null) {
            play(trackIndexed.getIndex());
        }
    }

    @Override
    public synchronized void pause() {
        if (currentTrack.get() != null) {
            currentTrack.get().pause();
        }
    }

    @Override
    public synchronized void resumeTrack() {
        if (currentTrack.get() != null) {
            currentTrack.get().resumeTrack();
        }
    }

    @Override
    public synchronized void stopTrack() {
        if (currentTrack.get() != null) {
            currentTrack.get().stopTrack();
        }
    }

    @Override
    public void reload() {
        if (rootFolder == null) {
            return;
        }

        playerStatusData.setCurrentTrackIndex(0);
        playerStatusData.setMute(false);
        playerStatusData.setOn(true);
        playerStatusData.setVolume(100.0f);

        loadTracks(rootFolder);
        play(playerStatusData.getCurrentTrackIndex());
    }

    // SeekedState o monitorear mejor el Playing?
    @Override
    public synchronized void seek(double seconds) {
        if (currentTrack.get() != null) {
            try {
                currentTrack.get().seek(seconds);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public synchronized void gotoSecond(double second) {
        if (currentTrack.get() != null) {
            try {
                currentTrack.get().gotoSecond(second);
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public synchronized float getVolume() {
        return currentTrack.get() == null
                ? playerStatusData.getVolume() : currentTrack.get().getVolume();
    }

    // 0-100
    @Override
    public synchronized void setVolume(float volume) {
        playerStatusData.setVolume(volume);
        if (currentTrack.get() != null) {
            currentTrack.get().setVolume(volume);
        }
    }

    @Override
    public synchronized void mute() {
        playerStatusData.setMute(true);
        if (currentTrack.get() != null) {
            currentTrack.get().mute();
        }
    }

    @Override
    public synchronized void unMute() {
        if (playerStatusData.isVolumeZero()) {
            playerStatusData.setVolume(100);
        } else {
            playerStatusData.setMute(false);
        }
        if (currentTrack.get() != null) {
            currentTrack.get().unMute();
        }
    }

    @Override
    public double getProgress() {
        return currentTrack.get() == null ? 0 : currentTrack.get().getProgress();
    }

    @Override
    public long getDuration() {
        return listTracks.stream().map(Track::getDuration)
                .reduce(Long::sum).orElse(0L);
    }

    @Override
    public synchronized void shutdown() {
        playerStatusData.setOn(false);
        interruptor.switchOn();
        this.interrupt();
    }

    @Override
    public float getSystemVolume() {
        return audioSystemManager.getConsoleMasterVolume();
    }

    @Override
    public void setSystemVolume(float volume) {
        audioSystemManager.setConsoleMasterVolume((int) volume);
    }

    @Override
    public void run() {
//        ThreadUtil.freezeThread(this);
        playerStatusData.setOn(true);

        while (playerStatusData.isOn() && !isInterrupted()) {
            interruptor.checkSignal();
        }

        Track track = currentTrack.get();
        if (track != null && track.isActive()) {
            track.kill();
        }

        listTracks.clear();
        listFolders.clear();
        listAudioFileScanners.clear();
    }

}
