package org.muplayer.thread;

import org.muplayer.audio.track.Track;
import org.muplayer.listener.ListenerMethodName;
import org.muplayer.listener.PlayerListener;

import java.util.List;

public class ListenerRunner implements Runnable {
    private final List<PlayerListener> listListeners;
    private final ListenerMethodName methodName;
    private final Track track;

    public ListenerRunner(List<PlayerListener> listListeners, ListenerMethodName methodName, Track track) {
        this.listListeners = listListeners;
        this.methodName = methodName;
        this.track = track;
    }

    @Override
    public void run() {
        switch (methodName) {
            case ON_SONG_CHANGE:
                listListeners.parallelStream()
                        .forEach(listener-> listener.onSongChange(track));
                break;
            case ON_PLAYED:
                listListeners.parallelStream()
                        .forEach(listener-> listener.onPlayed(track));
                break;
            case ON_PLAYING:
                listListeners.parallelStream()
                        .forEach(listener-> listener.onPlaying(track));
                break;
            case ON_RESUMED:
                listListeners.parallelStream()
                        .forEach(listener-> listener.onResumed(track));
                break;
            case ON_PAUSED:
                listListeners.parallelStream()
                        .forEach(listener-> listener.onPaused(track));
                break;
            case ON_STARTED:
                listListeners.parallelStream()
                        .forEach(PlayerListener::onStarted);
                break;
            case ON_STOPPED:
                listListeners.parallelStream()
                        .forEach(listener-> listener.onStopped(track));
                break;
            case ON_SOUGHT:
                listListeners.parallelStream()
                        .forEach(listener-> listener.onSeeked(track));
                break;
            case ON_SHUTDOWN:
                listListeners.parallelStream()
                        .forEach(PlayerListener::onShutdown);
                break;
        }
    }
}
