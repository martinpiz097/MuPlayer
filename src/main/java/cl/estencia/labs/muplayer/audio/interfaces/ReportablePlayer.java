package cl.estencia.labs.muplayer.audio.interfaces;

import cl.estencia.labs.muplayer.audio.model.Album;
import cl.estencia.labs.muplayer.audio.model.Artist;
import cl.estencia.labs.muplayer.audio.model.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.state.TrackStateName;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public interface ReportablePlayer {
    TrackStateName getCurrentTrackState();
    PlayerStatusData getPlayerStatusData();
    int getFoldersCount();
    int getSongsCount();
    AtomicReference<Track> getCurrentTrack();
    File getCurrentTrackFolder();
    int getCurrentFolderNumber();
    int getNextFolderNumber();
    int getPreviousFolderNumber();
    File getFolder(int number);
    Track getNext();
    Track getPrevious();
    File getRootFolder();
    List<Track> getTracks();
    List<File> getTrackFiles();
    List<File> getListFolders();
    List<Artist> getArtists();
    List<Album> getAlbums();
}
