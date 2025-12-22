package cl.estencia.labs.muplayer.bus.listener;

import cl.estencia.labs.muplayer.bus.model.MuPlayerResponse;

public interface PlayerResponseListener {
    void onPlayerResponse(MuPlayerResponse muPlayerResponse);
}

