package cl.estencia.labs.muplayer.listener.notifier.internal;

import cl.estencia.labs.muplayer.listener.TrackStateListener;
import cl.estencia.labs.muplayer.listener.event.TrackEvent;
import cl.estencia.labs.muplayer.listener.notifier.EventNotifier;

public class TrackInternalEventNotifier extends EventNotifier<TrackStateListener, TrackEvent> {
    public TrackInternalEventNotifier() {
        super();
    }

    @Override
    protected void onEventNotified(TrackStateListener listener, TrackEvent event) {
        listener.onStateChange(event);
    }

    @Override
    public void clearCustomObjects() {

    }
}
