package cl.estencia.labs.muplayer.audio.player;

import cl.estencia.labs.muplayer.audio.interfaces.ControllableMusic;
import cl.estencia.labs.muplayer.audio.interfaces.SystemVolumeController;
import cl.estencia.labs.muplayer.audio.model.Album;
import cl.estencia.labs.muplayer.audio.model.Artist;
import cl.estencia.labs.muplayer.audio.model.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.state.TrackStateName;
import cl.estencia.labs.muplayer.core.bus.listener.PlayerResponseListener;
import cl.estencia.labs.muplayer.audio.common.enums.SeekOption;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Player extends Thread implements ControllableMusic, SystemVolumeController {
    public abstract boolean isOn();
    public abstract boolean hasSounds();
    public abstract boolean isValidRootFolder();

    public abstract void addMusic(Collection<File> soundCollection);
    public abstract void addMusic(File musicFolder);
    public abstract void play(File track);
    public abstract void play(String trackName);

    public abstract void play(int trackIndex);
    public abstract void playFolder(String folderPath);
    public abstract void playFolder(int folderIndex);
    public abstract void playNext();
    public abstract void playPrevious();
    public abstract void seekFolder(SeekOption seekOption);
    public abstract void seekFolder(SeekOption seekOption, int jumps);
    public abstract void skipTracks(int skipCount, SeekOption option);

    public abstract void shutdown();

    public abstract void addResponseListener(PlayerResponseListener responseListener);
    public abstract void removeAllResponseListeners();

    public abstract TrackStateName getCurrentTrackState();
    public abstract PlayerStatusData getPlayerStatusData();
    public abstract int getFoldersCount();
    public abstract int getSongsCount();
    public abstract AtomicReference<Track> getCurrentTrack();
    public abstract File getCurrentTrackFolder();
    public abstract int getCurrentFolderNumber();
    public abstract int getNextFolderNumber();
    public abstract int getPreviousFolderNumber();
    public abstract File getFolder(int number);
    public abstract Track getNext();
    public abstract Track getPrevious();
    public abstract File getRootFolder();
    public abstract List<Track> getTracks();
    public abstract List<File> getTrackFiles();
    public abstract List<File> getListFolders();
    public abstract List<Artist> getArtists();
    public abstract List<Album> getAlbums();

}
