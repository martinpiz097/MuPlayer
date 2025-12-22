package cl.estencia.labs.muplayer.bus.model;

import cl.estencia.labs.muplayer.audio.player.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.track.Track;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MuPlayerResponse {
    private final Track currentTrack;
    private final PlayerStatusData playerStatusData;
}
