package cl.estencia.labs.muplayer.core.bus.model;

import cl.estencia.labs.muplayer.audio.common.enums.SeekOption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SkipData {
    private int skipCount;
    private SeekOption seekOption;

    public SkipData() {
        this.skipCount = 1;
        this.seekOption = SeekOption.NEXT;
    }
}
