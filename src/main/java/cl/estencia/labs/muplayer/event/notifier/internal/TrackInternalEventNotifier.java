package cl.estencia.labs.muplayer.event.notifier.internal;

import cl.estencia.labs.muplayer.event.listener.TrackStateListener;
import cl.estencia.labs.muplayer.event.model.TrackEvent;
import cl.estencia.labs.muplayer.event.notifier.EventNotifier;

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
