package cl.estencia.labs.muplayer.listener;

import cl.estencia.labs.muplayer.listener.event.PlayerEvent;

public interface PlayerListener {
    void onPreStart(PlayerEvent event);
    void onStarted(PlayerEvent event);
    void onUpdateTrackList(PlayerEvent event);
    void onCurrentTrackChange(PlayerEvent event);
    void onShutdown(PlayerEvent event);
    default void onEventReceived(PlayerEvent event) {
        switch (event.type()) {
            case PRE_START -> onPreStart(event);
            case START -> onStarted(event);
            case UPDATED_TRACK_LIST -> onUpdateTrackList(event);
            case CHANGED_CURRENT_TRACK -> onCurrentTrackChange(event);
            case SHUTDOWN -> onShutdown(event);
        }
    }
}
