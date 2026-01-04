package cl.estencia.labs.muplayer.core.bus.listener;

import cl.estencia.labs.muplayer.core.bus.model.MuPlayerResponse;

public interface PlayerResponseListener {
    void onPlayerResponse(MuPlayerResponse muPlayerResponse);
}

