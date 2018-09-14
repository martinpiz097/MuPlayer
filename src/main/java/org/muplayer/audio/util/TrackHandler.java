package org.muplayer.audio.util;

import org.muplayer.audio.Track;
import org.muplayer.audio.interfaces.MusicControls;

public class TrackHandler extends Thread implements MusicControls {

    private final Track track;

    public TrackHandler(Track track) {
        this.track = track;
        setName("TrackHandler: "+track.getTitle());
    }

    @Override
    public boolean isPlaying() throws Exception {
        return track.isPlaying();
    }

    @Override
    public boolean isPaused() throws Exception {
        return track.isPaused();
    }

    @Override
    public boolean isStopped() throws Exception {
        return track.isStopped();
    }

    @Override
    public boolean isFinished() throws Exception {
        return track.isFinished();
    }

    @Override
    public boolean isMute() {
        return track.isMute();
    }

    @Override
    public void play() throws Exception {
        if (isAlive())
            track.play();
        else
            start();
    }

    @Override
    public void pause() throws Exception {
        track.pause();
    }

    @Override
    public void resumeTrack() throws Exception {
        track.resumeTrack();
    }

    @Override
    public void stopTrack() throws Exception {
        track.stopTrack();
    }

    @Override
    public void finish() throws Exception {
        track.finish();
    }

    @Override
    public void seek(int seconds) throws Exception {
        track.seek(seconds);
    }

    @Override
    public float getGain() {
        return track.getGain();
    }

    @Override
    public void setGain(float volume) {
        track.setGain(volume);
    }

    @Override
    public void mute() {
        track.mute();
    }

    @Override
    public void unmute() {
        track.unmute();
    }

    @Override
    public int getProgress() {
        return track.getProgress();
    }

    @Override
    public void run() {
        track.run();
    }

}
