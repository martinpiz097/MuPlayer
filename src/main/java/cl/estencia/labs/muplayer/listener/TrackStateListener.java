package cl.estencia.labs.muplayer.listener;

import cl.estencia.labs.muplayer.listener.event.TrackEvent;

public interface TrackStateListener {
    void onStateChange(TrackEvent trackEvent);
}
