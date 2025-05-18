package cl.estencia.labs.muplayer.audio.interfaces;

import cl.estencia.labs.muplayer.audio.player.PlayerInfo;
import cl.estencia.labs.muplayer.audio.player.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.player.ReadableStatusData;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.state.TrackStateName;
import cl.estencia.labs.muplayer.audio.model.Album;
import cl.estencia.labs.muplayer.audio.model.Artist;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public interface ReportablePlayer {
    TrackStateName getCurrentTrackState();
    PlayerStatusData getPlayerStatusData();
    ReadableStatusData getStatusData();
    int getFoldersCount();
    int getSongsCount();
    AtomicReference<Track> getCurrentTrack();
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
