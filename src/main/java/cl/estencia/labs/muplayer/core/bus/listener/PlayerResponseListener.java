package cl.estencia.labs.muplayer.core.bus.listener;

import cl.estencia.labs.muplayer.core.bus.model.PlayerInfo;

public interface PlayerResponseListener {
    void onPlayerResponse(PlayerInfo playerInfo);
}

