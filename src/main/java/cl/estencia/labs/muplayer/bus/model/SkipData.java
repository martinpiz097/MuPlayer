package cl.estencia.labs.muplayer.v2.file.bus.model;

import cl.estencia.labs.muplayer.core.common.enums.SeekOption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
