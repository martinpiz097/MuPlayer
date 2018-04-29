package org.orangeplayer.audio.interfaces;

import org.orangeplayer.audio.Track;

public interface PlayerListener {
    public void onSongChange(Track newTrack);
    public void onPlayed(Track track);
    public void onResumed(Track track);
    public void onPaused(Track track);
    public void onStarted();
    public void onStopped(Track track);
    public void onSeeked(Track track);
    public void onShutdown();
}
