package cl.estencia.labs.muplayer.muplayer.interfaces;

import cl.estencia.labs.muplayer.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.muplayer.audio.track.state.TrackState;
import cl.estencia.labs.muplayer.muplayer.audio.player.PlayerInfo;
import cl.estencia.labs.muplayer.muplayer.model.Album;
import cl.estencia.labs.muplayer.muplayer.model.Artist;

import java.io.File;
import java.util.List;

public interface ReportablePlayer {
    TrackState getCurrentTrackState();
    int getFoldersCount();
    int getSongsCount();
    Track getCurrent();
    Track getNext();
    Track getPrevious();
    File getRootFolder();
    List<Track> getTracks();
    List<File> getListSoundFiles();
    List<File> getListFolders();
    List<Artist> getArtists();
    List<Album> getAlbums();
    PlayerInfo getInfo();
}
