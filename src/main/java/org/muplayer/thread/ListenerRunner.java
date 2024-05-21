package org.muplayer.thread;

import org.muplayer.audio.track.Track;
import org.muplayer.listener.PlayerListener;

import java.util.List;

import static org.muplayer.listener.ListenersNames.*;

public class ListenerRunner implements Runnable {
    private final List<PlayerListener> listListeners;
    private final String methodName;
    private final Track track;

    public ListenerRunner(List<PlayerListener> listListeners, String methodName, Track track) {
        this.listListeners = listListeners;
        this.methodName = methodName;
        this.track = track;
    }

    @Override
    public void run() {
        switch (methodName) {
            case ONSONGCHANGE:
                listListeners.parallelStream()
                        .forEach(listener-> listener.onSongChange(track));
                break;
            case ONPLAYED:
                listListeners.parallelStream()
                        .forEach(listener-> listener.onPlayed(track));
                break;
            case ONPLAYING:
                listListeners.parallelStream()
                        .forEach(listener-> listener.onPlaying(track));
                break;
            case ONRESUMED:
                listListeners.parallelStream()
                        .forEach(listener-> listener.onResumed(track));
                break;
            case ONPAUSED:
                listListeners.parallelStream()
                        .forEach(listener-> listener.onPaused(track));
                break;
            case ONSTARTED:
                listListeners.parallelStream()
                        .forEach(PlayerListener::onStarted);
                break;
            case ONSTOPPED:
                listListeners.parallelStream()
                        .forEach(listener-> listener.onStopped(track));
                break;
            case ONSEEKED:
                listListeners.parallelStream()
                        .forEach(listener-> listener.onSeeked(track));
                break;
            case ONSHUTDOWN:
                listListeners.parallelStream()
                        .forEach(PlayerListener::onShutdown);
                break;
        }
    }
}
