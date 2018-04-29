package org.orangeplayer.audio.interfaces;

public interface MusicControls {
    public boolean isPlaying() throws Exception;
    public boolean isPaused() throws Exception;
    public boolean isStoped() throws Exception;
    public boolean isFinished() throws Exception;

    public void play() throws Exception;
    public void pause() throws Exception;
    public void resumeTrack() throws Exception;
    public void stopTrack() throws Exception;
    public void finish() throws Exception;
    public void setGain(float volume) throws Exception;
    public void seek(int bytes) throws Exception;
}
