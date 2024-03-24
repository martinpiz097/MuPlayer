package org.muplayer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.muplayer.audio.track.Track;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackSearch {
    private Track track;
    private int index;
}
