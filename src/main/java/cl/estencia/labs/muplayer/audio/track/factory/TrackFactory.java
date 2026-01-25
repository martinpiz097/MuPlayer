package cl.estencia.labs.muplayer.audio.track.factory;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.config.model.MuPlayerConfigKeys;
import cl.estencia.labs.muplayer.config.reader.MuPlayerConfigReader;
import cl.estencia.labs.muplayer.console.common.enums.TrackFactoryType;
import cl.estencia.labs.muplayer.core.exception.AudioFileInvalidException;
import cl.estencia.labs.muplayer.core.exception.FormatNotSupportedException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public abstract class TrackFactory {

    protected TrackFactory() {
    }

    public Track getTrack(String dataSource) throws FormatNotSupportedException, AudioFileInvalidException {
        return getTrack(new File(dataSource));
    }

    public abstract Track getTrack(File dataSource) throws AudioFileInvalidException, FormatNotSupportedException;

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
