package cl.estencia.labs.muplayer.audio.track.factory;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.core.exception.FormatNotSupportedException;
import cl.estencia.labs.muplayer.core.exception.MuPlayerException;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;
import cl.estencia.labs.muplayer.util.TrackClassLoader;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

import static cl.estencia.labs.muplayer.util.FileUtil.getFileFormatName;

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
    public Track getTrack(File dataSource, TrackInternalEventNotifier internalEventNotifier) throws FormatNotSupportedException {
        if (dataSource != null && dataSource.exists()) {
            Track result = instanceTrackFromClass(dataSource, internalEventNotifier);
            if (result == null) {
                throw new FormatNotSupportedException(
                        getFileFormatName(dataSource.getName()));
            }

            return result;
        } else {
            throw new MuPlayerException("The dataSource object is null or not exists");
        }
    }
}
