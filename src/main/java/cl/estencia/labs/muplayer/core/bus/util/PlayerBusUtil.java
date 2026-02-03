package cl.estencia.labs.muplayer.core.bus.util;

import cl.estencia.labs.ebot.bus.MessageBus;

public class PlayerBusUtil {
    private static MessageBus newPlayerBus() {
        return MessageBus.multiPublisherPerTopic(1, false);
    }

    private static void initPlayerBus(MessageBus messageBus) {
        if (!messageBus.isAlive()) {
            messageBus.start();
        }
    }

    /*private static MessageBus createPlayerBus() {
        shutdownPlayerBus();
        return CACHE.set(PLAYER_BUS, newPlayerBus());
    }*/

    public static MessageBus getPlayerBus() {
        /*MessageBus messageBus = CACHE.get(PLAYER_BUS, PLAYER_BUS.class);
        if (messageBus == null) {
            messageBus = createPlayerBus();
            initPlayerBus(messageBus);
        }*/
        MessageBus messageBus = newPlayerBus();
        initPlayerBus(messageBus);

        return messageBus;
    }

//    public static void removePlayerBus() {
//        CACHE.remove(PLAYER_BUS);
//    }

    /* public static void shutdownPlayerBus() {
        MessageBus messageBus = CACHE.get(PLAYER_BUS, PLAYER_BUS.class);
        if (messageBus != null && PLAYER_BUS.isAlive()) {
            PLAYER_BUS.shutdown();
        }
    } */

}
