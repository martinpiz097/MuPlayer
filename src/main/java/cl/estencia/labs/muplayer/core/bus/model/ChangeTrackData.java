package cl.estencia.labs.muplayer.core.bus.model;

import java.io.File;

public class ChangeTrackData {
    private final File newTrackFile;

    public ChangeTrackData(File newTrackFile) {
        this.newTrackFile = newTrackFile;
    }
}
