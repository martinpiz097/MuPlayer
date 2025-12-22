package cl.estencia.labs.muplayer.audio.player;

import cl.estencia.labs.muplayer.audio.interfaces.ControllableMusic;
import cl.estencia.labs.muplayer.audio.interfaces.ReportablePlayer;
import cl.estencia.labs.muplayer.audio.interfaces.SystemVolumeController;
import cl.estencia.labs.muplayer.bus.listener.PlayerResponseListener;
import cl.estencia.labs.muplayer.core.common.enums.SeekOption;

import java.io.File;
import java.util.Collection;

public abstract class Player extends Thread implements ControllableMusic, ReportablePlayer, SystemVolumeController {
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
    public abstract void skipTracks(int skipCount, SeekOption option);

    public abstract void shutdown();

    public abstract void addResponseListener(PlayerResponseListener responseListener);
    public abstract void removeAllResponseListeners();

}
