package cl.estencia.labs.muplayer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import cl.estencia.labs.muplayer.audio.track.Track;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackIndexed {
    private Track track;
    private int index;
}
