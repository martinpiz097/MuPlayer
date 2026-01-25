package cl.estencia.labs.muplayer.core.bus.util;

import cl.estencia.labs.ebot.bus.MessageBus;
import cl.estencia.labs.muplayer.core.cache.CacheManager;

import static cl.estencia.labs.muplayer.core.cache.CacheManager.GLOBAL_CACHE;
import static cl.estencia.labs.muplayer.core.cache.CacheVar.MESSAGE_BUS;

public class MessageBusUtil {
    private static MessageBus newMessageBus() {
        return MessageBus.multiPublisherPerTopic(1, false);
    }

    private static void initMessageBus(MessageBus messageBus) {
        if (!messageBus.isAlive()) {
            messageBus.start();
        }
    }

    private static MessageBus createMessageBus() {
        shutdownMessageBus();
        return GLOBAL_CACHE.saveValue(MESSAGE_BUS, newMessageBus());
    }

    public static MessageBus getMessageBus() {
        MessageBus messageBus = GLOBAL_CACHE.loadValue(MESSAGE_BUS, MessageBus.class);
        if (messageBus == null) {
            messageBus = createMessageBus();
            initMessageBus(messageBus);
        }

        return messageBus;
    }

    public static void removeMessageBus() {
        GLOBAL_CACHE.removeValue(MESSAGE_BUS);
    }

    public static void shutdownMessageBus() {
        MessageBus messageBus = GLOBAL_CACHE.loadValue(MESSAGE_BUS, MessageBus.class);
        if (messageBus != null && messageBus.isAlive()) {
            messageBus.shutdown();
        }
    }

}
