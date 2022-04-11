package org.muplayer.audio.interfaces;

import org.muplayer.audio.Track;

public interface PlayerListener {
    void onSongChange(Track newTrack);
    void onPlayed(Track track);
    void onPlaying(Track track);
    void onResumed(Track track);
    void onPaused(Track track);
    void onStarted();
    void onStopped(Track track);
    void onSeeked(Track track);
    void onGotosecond(Track track);
    void onShutdown();
}
