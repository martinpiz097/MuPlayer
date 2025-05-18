package cl.estencia.labs.muplayer.audio.player;

import cl.estencia.labs.muplayer.audio.interfaces.ReportablePlayer;
import cl.estencia.labs.muplayer.audio.interfaces.SystemAudioController;
import cl.estencia.labs.muplayer.event.listener.PlayerListener;
import cl.estencia.labs.muplayer.event.listener.TrackStateListener;
import cl.estencia.labs.muplayer.interfaces.ControllableMusic;
import cl.estencia.labs.muplayer.core.common.enums.SeekOption;

import java.io.File;
import java.util.Collection;
import java.util.List;

public abstract class Player extends Thread implements ControllableMusic, ReportablePlayer, SystemAudioController {
    public abstract boolean isOn();
    public abstract boolean hasSounds();

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
    public abstract void jumpTrack(int jumps, SeekOption option);

    public abstract void shutdown();

    public abstract void addPlayerListener(PlayerListener listener);
    public abstract void addTrackListener(TrackStateListener trackStateListener);
    public abstract List<PlayerListener> getPlayerListeners();
    public abstract List<TrackStateListener> getTrackListeners();
    public abstract void removePlayerListener(PlayerListener reference);
    public abstract void removeTrackListener(TrackStateListener trackStateListener);
    public abstract void removeAllPlayerListeners();
    public abstract void removeAllTrackListeners();
    public void removeAllListeners() {
        removeAllTrackListeners();
        removeAllPlayerListeners();
    }
    //public abstract void reloadTracks();

}
