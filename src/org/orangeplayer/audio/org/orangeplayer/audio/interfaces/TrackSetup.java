package org.orangeplayer.audio.org.orangeplayer.audio.interfaces;

public interface TrackSetup {
    public abstract boolean isPlaying() throws Exception;
    public abstract boolean isPaused() throws Exception;
    public abstract boolean isStoped() throws Exception;
    public abstract boolean isFinished() throws Exception;
    public abstract void play() throws Exception;
    public abstract void pause() throws Exception;
    public abstract void resume() throws Exception;
    public abstract void stop() throws Exception;
    public abstract void finish() throws Exception;
    public abstract void seek(int seconds) throws Exception;
}
