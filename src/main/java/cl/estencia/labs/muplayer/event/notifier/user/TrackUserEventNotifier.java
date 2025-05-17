package cl.estencia.labs.muplayer.event.notifier.user;

import cl.estencia.labs.muplayer.event.listener.TrackStateListener;
import cl.estencia.labs.muplayer.event.model.TrackEvent;
import cl.estencia.labs.muplayer.event.notifier.EventNotifier;

public class TrackUserEventNotifier extends EventNotifier<TrackStateListener, TrackEvent> {

    public TrackUserEventNotifier() {
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
