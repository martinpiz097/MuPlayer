package org.muplayer.interfaces;

import org.muplayer.audio.Track;
import org.muplayer.audio.trackstates.TrackState;
import org.muplayer.info.PlayerInfo;
import org.muplayer.model.Album;
import org.muplayer.model.Artist;
import org.muplayer.model.ReportableTrack;

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
    List<ReportableTrack> getTracksInfo();
    List<Artist> getArtists();
    List<Album> getAlbums();
    PlayerInfo getInfo();
}
