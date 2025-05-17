package cl.estencia.labs.muplayer.listener.notifier.user;

import cl.estencia.labs.muplayer.exception.MuPlayerException;
import cl.estencia.labs.muplayer.listener.PlayerListener;
import cl.estencia.labs.muplayer.listener.event.PlayerEvent;
import cl.estencia.labs.muplayer.listener.notifier.EventNotifier;

public class PlayerUserEventNotifier extends EventNotifier<PlayerListener, PlayerEvent> {
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
