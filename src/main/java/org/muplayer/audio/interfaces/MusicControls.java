package org.muplayer.audio.interfaces;

public interface MusicControls {
    public boolean isPlaying() throws Exception;
    public boolean isPaused() throws Exception;
    public boolean isStopped() throws Exception;
    public boolean isFinished() throws Exception;
    public boolean isMute();

    public void play() throws Exception;
    public void pause() throws Exception;
    public void resumeTrack() throws Exception;
    public void stopTrack() throws Exception;
    public void finish() throws Exception;
    public void seek(double seconds) throws Exception;
    public void gotoSecond(double second) throws Exception;
    public float getGain();
    public void setGain(float volume);
    public void mute();
    public void unmute();

    public double getProgress();
}
