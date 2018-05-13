package org.orangeplayer.audio.interfaces;

import java.io.File;
import java.util.List;

public interface PlayerControls extends MusicControls {
    public void open(File sound);
    public void open(List<File> listSounds);
    public void addMusic(List<File> listSounds);
    public void addMusic(File musicFolder);
    public void playNext();
    public void playPrevious();
    public void shutdown();
}
