package cl.estencia.labs.muplayer.model;

import cl.estencia.labs.muplayer.audio.player.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.track.Track;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class MuPlayerResponse {
    private final Track currentTrack;
    private final PlayerStatusData playerStatusData;
}
