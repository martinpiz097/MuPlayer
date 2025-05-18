package cl.estencia.labs.muplayer.event.model;

import cl.estencia.labs.muplayer.audio.interfaces.TrackData;
import cl.estencia.labs.muplayer.audio.track.state.TrackStateName;

public record TrackEvent (TrackData trackData, TrackStateName trackStateName) {}

