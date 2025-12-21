package cl.estencia.labs.muplayer.audio.model;

import cl.estencia.labs.muplayer.audio.track.Track;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackIndexed {
    private Track track;
    private int index;
}
