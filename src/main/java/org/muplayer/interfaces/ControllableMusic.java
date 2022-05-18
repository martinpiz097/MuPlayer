package org.muplayer.interfaces;

import org.muplayer.audio.info.AudioHardware;
import org.muplayer.system.Time;

public interface ControllableMusic {
    boolean isPlaying() throws Exception;
    boolean isPaused() throws Exception;
    boolean isStopped() throws Exception;
    boolean isFinished() throws Exception;
    boolean isMute();

    void play() throws Exception;
    void pause() throws Exception;
    void resumeTrack() throws Exception;
    void stopTrack() throws Exception;
    void finish() throws Exception;
    void seek(double seconds) throws Exception;
    void gotoSecond(double second) throws Exception;
    float getGain();
    void setGain(float volume);
    default float getSystemVolume() {
        return AudioHardware.getFormattedMasterVolume();
    }
    default void setSystemVolume(float volume) {
        AudioHardware.setFormattedMasterVolume(volume);
    }
    void mute();
    void unMute();

    double getProgress();
    default String getFormattedProgress() {
        return Time.getInstance().getTimeFormatter().format((long) getProgress());
    }

    long getDuration();
    default String getFormattedDuration() {
        return Time.getInstance().getTimeFormatter().format(getDuration());
    }

}
