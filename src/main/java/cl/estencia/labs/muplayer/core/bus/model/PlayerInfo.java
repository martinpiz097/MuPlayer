package cl.estencia.labs.muplayer.core.bus.model;

import cl.estencia.labs.muplayer.audio.model.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.track.Track;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerInfo {
    private final Track currentTrack;
    private final PlayerStatusData playerStatusData;
}
