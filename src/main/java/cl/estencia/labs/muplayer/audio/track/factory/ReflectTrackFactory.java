package cl.estencia.labs.muplayer.audio.track;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.exception.FormatNotSupportedException;
import cl.estencia.labs.muplayer.exception.MuPlayerException;
import cl.estencia.labs.muplayer.util.TrackClassLoader;

import java.io.File;
import java.lang.reflect.Constructor;

import static cl.estencia.labs.muplayer.util.FileUtil.getFileFormatName;

public class ReflectTrackFactory implements TrackFactory {
    private final TrackClassLoader trackClassLoader;

    public ReflectTrackFactory() {
        this.trackClassLoader = new TrackClassLoader();
    }

    private Track instanceTrackFromClass(File dataSource, Player player) {
        var listInitConstructors = trackClassLoader.getListInitConstructors();

        Track track = null;
        for (Constructor<? extends Track> initConstructor : listInitConstructors) {
            track = trackClassLoader.tryInstance(initConstructor,
                    dataSource, player);
            if (track != null) {
                break;
            }
        }

        return track;
    }

    @Override
    public Track getTrack(File dataSource, Player player) {
        if (dataSource != null && dataSource.exists()) {
            Track result = instanceTrackFromClass(dataSource, player);
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
