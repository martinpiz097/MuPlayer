package cl.estencia.labs.muplayer.audio.player;

import cl.estencia.labs.ebot.bus.model.message.Message;
import cl.estencia.labs.ebot.bus.model.pubsub.sub.MessageListener;
import cl.estencia.labs.muplayer.core.bus.listener.PlayerResponseListener;
import cl.estencia.labs.muplayer.core.bus.message.MuPlayerTopic;

public abstract class EventPlayer extends Thread {
    protected abstract void configureListeners();
    public abstract void sendEvent(Message message);
    public abstract void addListener(MuPlayerTopic topic, MessageListener listener);
    public abstract void addResponseListener(PlayerResponseListener responseListener);
    public abstract void removeAllListeners();
    public abstract void removeAllResponseListeners();
}
