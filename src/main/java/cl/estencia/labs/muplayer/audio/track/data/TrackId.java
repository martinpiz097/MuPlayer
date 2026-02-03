package cl.estencia.labs.muplayer.audio.track.data;

import lombok.Getter;
import lombok.ToString;

import java.io.File;
import java.util.UUID;

@Getter
@ToString
public class TrackId {
    private final String sequenceId;

    public TrackId(File dataSource) {
        this.sequenceId = UUID.randomUUID().toString();
    }

}
