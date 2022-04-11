package org.muplayer.audio.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.muplayer.audio.Track;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackSearch {
    private Track track;
    private int index;
}
