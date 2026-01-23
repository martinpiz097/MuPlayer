package cl.estencia.labs.muplayer.audio.track.factory;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.core.exception.AudioFileInvalidException;
import cl.estencia.labs.muplayer.core.exception.FormatNotSupportedException;
import cl.estencia.labs.muplayer.core.exception.MuPlayerException;
import cl.estencia.labs.muplayer.core.util.TrackClassLoader;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

import static cl.estencia.labs.muplayer.audio.util.AudioFileUtil.getFileFormatName;

public class ReflectTrackFactory implements TrackFactory {
    private final TrackClassLoader trackClassLoader;

    public ReflectTrackFactory() {
        this.trackClassLoader = new TrackClassLoader();
    }

    private Optional<Track> instanceTrackFromClass(Object... parameter) {
        var listInitConstructors = trackClassLoader.getListInitConstructors();

        return listInitConstructors.parallelStream()
                .map(initConstructor ->
                        (Track) trackClassLoader.tryInstance(initConstructor, parameter))
                .filter(Objects::nonNull)
                .findFirst();
    }

    @Override
    public Track getTrack(File dataSource) throws FormatNotSupportedException, AudioFileInvalidException {
        if (dataSource == null || !dataSource.exists() || dataSource.isDirectory()) {
            throw new AudioFileInvalidException(dataSource);
        }

        Optional<Track> result = instanceTrackFromClass(dataSource);
        if (result.isEmpty()) {
            throw new FormatNotSupportedException(getFileFormatName(dataSource.getName()));
        }

        return result.get();
    }
}
