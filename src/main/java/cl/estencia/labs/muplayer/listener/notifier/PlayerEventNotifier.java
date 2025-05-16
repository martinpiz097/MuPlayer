package cl.estencia.labs.muplayer.listener.notifier;

import cl.estencia.labs.muplayer.listener.PlayerListener;
import cl.estencia.labs.muplayer.listener.event.PlayerEvent;
import lombok.extern.java.Log;

@Log
public class PlayerEventNotifier extends EventNotifier<PlayerListener, PlayerEvent> {
    @Override
    protected void onEventNotified(PlayerListener listener, PlayerEvent event) {
        listener.onEventReceived(event);
    }
}
