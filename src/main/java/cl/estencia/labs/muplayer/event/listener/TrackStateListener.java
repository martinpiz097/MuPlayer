package cl.estencia.labs.muplayer.event.listener;

import cl.estencia.labs.muplayer.event.model.TrackEvent;

public interface TrackStateListener {
    void onStateChange(TrackEvent trackEvent);
}
