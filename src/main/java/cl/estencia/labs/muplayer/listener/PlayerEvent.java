package cl.estencia.labs.muplayer.listener;

import cl.estencia.labs.muplayer.audio.track.Track;

import java.io.File;
import java.util.List;

public interface PlayerEvent {
    void onStarted();
    void onUpdateTrackList(List<Track> listTracks, List<File> listFolders);
    void onShutdown();
}
