package cl.estencia.labs.muplayer.muplayer.interfaces;

import cl.estencia.labs.muplayer.muplayer.system.Time;

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
