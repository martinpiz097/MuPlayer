package cl.estencia.labs.muplayer.event.listener;

import cl.estencia.labs.muplayer.event.model.PlayerEvent;

import java.io.FileNotFoundException;

public interface PlayerListener {
    void onPreStart(PlayerEvent event) throws FileNotFoundException;
    void onStarted(PlayerEvent event) throws FileNotFoundException;
    void onUpdateTrackList(PlayerEvent event);
    void onCurrentTrackChange(PlayerEvent event);
    void onShutdown(PlayerEvent event);
    default void onEventReceived(PlayerEvent event) throws FileNotFoundException {
        switch (event.type()) {
            case PRE_START -> onPreStart(event);
            case START -> onStarted(event);
            case UPDATED_TRACK_LIST -> onUpdateTrackList(event);
            case CHANGED_CURRENT_TRACK -> onCurrentTrackChange(event);
            case SHUTDOWN -> onShutdown(event);
        }
    }
}
