package cl.estencia.labs.muplayer.listener.event;

import cl.estencia.labs.muplayer.audio.track.state.TrackStateName;
import cl.estencia.labs.muplayer.interfaces.TrackData;

public record TrackEvent (TrackData trackData, TrackStateName trackStateName) {}

