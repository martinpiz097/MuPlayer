package org.muplayer.audio.interfaces;

import java.io.File;
import java.util.List;

public interface PlayerControls extends MusicControls {
    void open(File sound);
    void open(List<File> listSounds);
    void addMusic(List<File> listSounds);
    void addMusic(File musicFolder);
    void play(File track);
    void play(String trackName);
    void playNext();
    void playPrevious();
    void shutdown();

    float getSystemVolume();
    void setSystemVolume(float volume);
}
