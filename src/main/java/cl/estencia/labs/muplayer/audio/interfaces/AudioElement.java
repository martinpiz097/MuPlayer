package cl.estencia.labs.muplayer.audio.interfaces;

import cl.estencia.labs.muplayer.core.system.Time;

public interface AudioElement {
    boolean isPlaying();
    boolean isPaused();
    boolean isStopped();
    boolean isMute();

    void play();
    void pause();
    void resumeTrack();
    void stopTrack();
    void reload();
    void seek(double seconds);
    void gotoSecond(double second);
    float getVolume();
    void setVolume(float volume);
    void mute();
    void unMute();
    /*default void muteSystemVolume() {
        audioHardware.setSpeakerMuteValue(true);
    }
    default void unmuteSystemVolume() {
        audioHardware.setSpeakerMuteValue(false);
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
