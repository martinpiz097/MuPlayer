package org.muplayer.interfaces;

import org.muplayer.audio.info.AudioHardware;
import org.muplayer.system.Time;

public interface ControllableMusic {
    boolean isPlaying() throws Exception;
    boolean isPaused() throws Exception;
    boolean isStopped() throws Exception;
    boolean isMute();

    void play() throws Exception;
    void pause() throws Exception;
    void resumeTrack() throws Exception;
    void stopTrack() throws Exception;
    void reload() throws Exception;
    void seek(double seconds) throws Exception;
    void gotoSecond(double second) throws Exception;
    float getVolume();
    void setVolume(float volume);
    default float getSystemVolume() {
        return AudioHardware.getFormattedSpeakerVolume();
    }
    default void setSystemVolume(float volume) {
        AudioHardware.setFormattedSpeakerVolume(volume);
    }
    void mute();
    void unMute();
    /*default void muteSystemVolume() {
        AudioHardware.setSpeakerMuteValue(true);
    }
    default void unmuteSystemVolume() {
        AudioHardware.setSpeakerMuteValue(false);
    }*/

    double getProgress();
    default String getFormattedProgress() {
        return Time.getInstance().getTimeFormatter().format((long) getProgress());
    }

    long getDuration();
    default String getFormattedDuration() {
        return Time.getInstance().getTimeFormatter().format(getDuration());
    }

}
