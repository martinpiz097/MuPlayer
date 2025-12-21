package cl.estencia.labs.muplayer.v2.file.bus.model;

import java.io.File;

public class ChangeTrackData {
    private final File newTrackFile;

    public ChangeTrackData(File newTrackFile) {
        this.newTrackFile = newTrackFile;
    }
}
