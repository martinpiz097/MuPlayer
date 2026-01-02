package cl.estencia.labs.muplayer.bus;

import cl.estencia.labs.ebot.bus.MessageBus;
import cl.estencia.labs.muplayer.core.cache.CacheManager;

import static cl.estencia.labs.muplayer.core.cache.CacheVar.MESSAGE_BUS;

public class MuPlayerBusUtil {
    private static final CacheManager GLOBAL_CACHE_MANAGER = CacheManager.getGlobalCache();

    private static MessageBus newMessageBus() {
        return MessageBus.multiPublisherPerTopic(1, false);
    }

    private static void shutdownCurrentBus() {
        MessageBus messageBus = GLOBAL_CACHE_MANAGER.loadValue(MESSAGE_BUS, MessageBus.class);
        if (messageBus != null && messageBus.isAlive()) {
            messageBus.shutdown();
        }
    }

    private static void initMessageBus(MessageBus messageBus) {
        if (!messageBus.isAlive()) {
            messageBus.start();
        }
    }

    private static MessageBus createMessageBus() {
        shutdownCurrentBus();
        return GLOBAL_CACHE_MANAGER.saveValue(MESSAGE_BUS, newMessageBus());
    }

    public static MessageBus getMessageBus() {
        MessageBus messageBus = GLOBAL_CACHE_MANAGER.loadValue(MESSAGE_BUS, MessageBus.class);
        if (messageBus == null) {
            messageBus = createMessageBus();
            initMessageBus(messageBus);
        }

        return messageBus;
    }

}
