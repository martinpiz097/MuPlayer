package cl.estencia.labs.muplayer.model;

import cl.estencia.labs.muplayer.audio.track.Track;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackIndexed {
    private Track track;
    private int index;
}
