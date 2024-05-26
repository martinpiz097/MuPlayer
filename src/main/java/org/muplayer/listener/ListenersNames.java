package org.muplayer.listener;

public class ListenersNames {

    private ListenersNames() {
        throw new IllegalStateException("Utility class");
    }

    public static final String ON_STARTED = "onStarted";
    public static final String ON_SHUTDOWN = "onShutdown";
    public static final String ON_SONG_CHANGE = "onSongChange";
    public static final String ON_PLAYING = "onPlaying";
    public static final String ON_PLAYED = "onPlayed";
    public static final String ON_RESUMED = "onResumed";
    public static final String ON_PAUSED = "onPaused";
    public static final String ON_STOPPED = "onStopped";
    public static final String ON_SOUGHT = "onSought";
    public static final String ON_GO_TO_SECOND = "onGoToSecond";
}
