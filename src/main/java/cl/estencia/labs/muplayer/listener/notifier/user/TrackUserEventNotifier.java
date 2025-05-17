package cl.estencia.labs.muplayer.listener.notifier.user;

import cl.estencia.labs.muplayer.listener.TrackStateListener;
import cl.estencia.labs.muplayer.listener.event.TrackEvent;
import cl.estencia.labs.muplayer.listener.notifier.EventNotifier;

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
