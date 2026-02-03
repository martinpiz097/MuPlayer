package cl.estencia.labs.muplayer.audio.track.factory;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.config.model.MuPlayerConfigKeys;
import cl.estencia.labs.muplayer.console.common.enums.TrackFactoryType;

import java.io.File;

import static cl.estencia.labs.muplayer.config.reader.ResourceReaders.MUPLAYER_CONFIG_READER;

public abstract class TrackFactory {
    private static final TrackFactoryType FACTORY_TYPE = readFactoryType();

    private static TrackFactoryType readFactoryType() {
        String factoryTypeName = MUPLAYER_CONFIG_READER
                .getProperty(MuPlayerConfigKeys.TRACK_FACTORY_TYPE);
        if (factoryTypeName == null || factoryTypeName.isBlank()) {
            return null;
        }

        return TrackFactoryType.valueOf(factoryTypeName);
    }

    public static TrackFactory newFactory() {
        if (FACTORY_TYPE == null) {
            return null;
        }

        return switch (FACTORY_TYPE) {
            case STANDARD -> new StandardTrackFactory();
            case REFLECTION -> null;
        };
    }

    protected TrackFactory() {
    }

    public Track loadTrack(String dataSource) {
        return loadTrack(new File(dataSource));
    }

    public abstract Track loadTrack(File dataSource);

}
