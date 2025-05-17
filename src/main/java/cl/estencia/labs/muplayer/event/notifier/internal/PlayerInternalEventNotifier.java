package cl.estencia.labs.muplayer.event.notifier.internal;

import cl.estencia.labs.muplayer.event.model.PlayerEvent;
import cl.estencia.labs.muplayer.core.exception.MuPlayerException;
import cl.estencia.labs.muplayer.event.listener.PlayerListener;
import cl.estencia.labs.muplayer.event.notifier.EventNotifier;

public class PlayerInternalEventNotifier extends EventNotifier<PlayerListener, PlayerEvent> {
    @Override
    protected void onEventNotified(PlayerListener listener, PlayerEvent event) {
        try {
            listener.onEventReceived(event);
        } catch (Exception e) {
            throw new MuPlayerException(e);
        }
    }

    @Override
    public void clearCustomObjects() {

    }
}
