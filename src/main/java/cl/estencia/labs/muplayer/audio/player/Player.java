package cl.estencia.labs.muplayer.audio.player;

import cl.estencia.labs.muplayer.interfaces.ControllableMusic;
import cl.estencia.labs.muplayer.listener.PlayerListener;
import cl.estencia.labs.muplayer.interfaces.ReportablePlayer;
import cl.estencia.labs.muplayer.model.SeekOption;

import java.io.File;
import java.util.Collection;
import java.util.List;

public abstract class Player extends AudioComponent implements ControllableMusic, ReportablePlayer {
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
    public abstract List<PlayerListener> getListeners();
    public abstract void removePlayerListener(PlayerListener reference);
    public abstract void removeAllListeners();
    //public abstract void reloadTracks();

}
