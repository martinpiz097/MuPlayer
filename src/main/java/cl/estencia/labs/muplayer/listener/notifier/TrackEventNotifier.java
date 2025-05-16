package cl.estencia.labs.muplayer.listener.notifier;

import cl.estencia.labs.muplayer.listener.TrackStateListener;
import cl.estencia.labs.muplayer.listener.event.TrackEvent;
import lombok.extern.java.Log;

@Log
public class TrackEventNotifier extends EventNotifier<TrackStateListener, TrackEvent> {
    public TrackEventNotifier() {
        super();
    }

    @Override
    protected void onEventNotified(TrackStateListener listener, TrackEvent event) {
        listener.onStateChange(event);
    }
}
