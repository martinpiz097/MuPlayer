package cl.estencia.labs.muplayer.listener;

import cl.estencia.labs.muplayer.audio.track.state.TrackState;
import cl.estencia.labs.muplayer.audio.track.state.TrackStateName;
import cl.estencia.labs.muplayer.interfaces.TrackData;

public interface TrackEvent {
    void onStateChange(TrackData trackData, TrackStateName stateName);
}
