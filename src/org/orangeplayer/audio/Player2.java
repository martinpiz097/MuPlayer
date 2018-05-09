package org.orangeplayer.audio;

import org.orangeplayer.audio.interfaces.PlayerControls;

import java.io.File;
import java.util.List;

public class Player2 implements PlayerControls {
    @Override
    public void open(File sound) {

    }

    @Override
    public void open(List<File> listSounds) {

    }

    @Override
    public void playNext() {

    }

    @Override
    public void playPrevious() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean isPlaying() throws Exception {
        return false;
    }

    @Override
    public boolean isPaused() throws Exception {
        return false;
    }

    @Override
    public boolean isStoped() throws Exception {
        return false;
    }

    @Override
    public boolean isFinished() throws Exception {
        return false;
    }

    @Override
    public void play() throws Exception {

    }

    @Override
    public void pause() throws Exception {

    }

    @Override
    public void resumeTrack() throws Exception {

    }

    @Override
    public void stopTrack() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }

    @Override
    public void setGain(float volume) throws Exception {

    }

    @Override
    public void seek(int bytes) throws Exception {

    }
}
