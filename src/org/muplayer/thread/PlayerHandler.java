package org.muplayer.thread;

import org.muplayer.audio.Player;

public class PlayerHandler {
    private volatile Player player;

    private static PlayerHandler handler;

    public static boolean hasInstance() {
        return handler != null;
    }

    public static void setInstance(Player player) {
        handler = new PlayerHandler(player);
    }

    public static PlayerHandler getInstance() {
        return handler;
    }

    public static synchronized Player getPlayer() {
        return handler.player;
    }

    private PlayerHandler(Player player) {
        this.player = player;
    }

    public void unfreezePlayerThread() {
        if (player != null)
            player.interrupt();
    }

}
