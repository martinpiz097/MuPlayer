package cl.estencia.labs.muplayer.audio.model;

import cl.estencia.labs.muplayer.audio.track.Track;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackIndexed {
    private Track track;
    private int index;
}
