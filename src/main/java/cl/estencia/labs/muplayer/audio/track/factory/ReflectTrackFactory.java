package cl.estencia.labs.muplayer.audio.track.factory;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.util.TrackClassLoader;
import cl.estencia.labs.muplayer.core.exception.FormatNotSupportedException;
import cl.estencia.labs.muplayer.core.exception.MuPlayerException;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

import static cl.estencia.labs.muplayer.audio.util.AudioFileUtil.getFileFormatName;

public class ReflectTrackFactory implements TrackFactory {
    private final TrackClassLoader trackClassLoader;

    public ReflectTrackFactory() {
        this.trackClassLoader = new TrackClassLoader();
    }

    private Track instanceTrackFromClass(Object... parameter) {
        var listInitConstructors = trackClassLoader.getListInitConstructors();

        Optional<Track> instance = listInitConstructors.parallelStream()
                .map(initConstructor ->
                        (Track) trackClassLoader.tryInstance(initConstructor, parameter))
                .filter(Objects::nonNull)
                .findFirst();

        return instance.orElse(null);
    }

    @Override
    public Track getTrack(File dataSource) throws FormatNotSupportedException {
        if (dataSource != null && dataSource.exists()) {
            Track result = instanceTrackFromClass(dataSource);
            if (result == null) {
                throw new FormatNotSupportedException(getFileFormatName(dataSource.getName()));
            }

            return result;
        } else {
            throw new MuPlayerException("The dataSource object is null or not exists");
        }
    }
}
