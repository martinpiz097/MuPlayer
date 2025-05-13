package cl.estencia.labs.muplayer.listener;

import cl.estencia.labs.muplayer.audio.track.Track;

public interface PlayerListener extends TrackListener {
    void onStarted();
    void onPlaying(Track track);
    void onPaused(Track track);
    void onResumed(Track track);
    void onStopped(Track track);
    void onSeek(Track track);
    void onGotoSecond(Track track);
    void onShutdown();
}
