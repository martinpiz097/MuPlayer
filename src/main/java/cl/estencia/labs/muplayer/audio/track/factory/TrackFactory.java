package cl.estencia.labs.muplayer.audio.track.factory;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.config.model.MuPlayerConfigKeys;
import cl.estencia.labs.muplayer.config.reader.MuPlayerConfigReader;
import cl.estencia.labs.muplayer.console.common.enums.TrackFactoryType;

import java.io.File;

public abstract class TrackFactory {

    protected TrackFactory() {
    }

    public Track loadTrack(String dataSource) {
        return loadTrack(new File(dataSource));
    }

    public abstract Track loadTrack(File dataSource);

    public static TrackFactory newFactory() {
        String factoryTypeName = MuPlayerConfigReader.getInstance()
                .getProperty(MuPlayerConfigKeys.TRACK_FACTORY_TYPE);
        TrackFactoryType factoryType = TrackFactoryType.valueOf(factoryTypeName);

        return switch (factoryType) {
            case STANDARD -> new StandardTrackFactory();
            case REFLECTION -> null;
        };

    }

}
