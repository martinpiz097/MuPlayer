package org.muplayer.audio.interfaces;

import org.muplayer.audio.model.SeekOption;

import java.io.File;
import java.util.Collection;

public interface PlayerControls extends MusicControls {

    boolean isOn();
    boolean hasSounds();

    void turnOn();
    void addMusic(Collection<File> soundCollection);
    void addMusic(File musicFolder);
    void play(File track);
    void play(String trackName);

    void play(int trackIndex);

    void playFolder(String folderPath);
    void playFolder(int folderIndex);
    void playNext();
    void playPrevious();

    void seekFolder(SeekOption seekOption);
    void seekFolder(SeekOption seekOption, int jumps);
    void shutdown();

    float getSystemVolume();
    void setSystemVolume(float volume);

}
