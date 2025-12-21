package cl.estencia.labs.muplayer.v2.file.bus.model;

import cl.estencia.labs.muplayer.core.common.enums.SeekOption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SkipTrackData {
    private final int skipCount;
    private final SeekOption seekOption;
}
