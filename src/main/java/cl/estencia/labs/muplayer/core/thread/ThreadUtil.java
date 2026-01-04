package cl.estencia.labs.muplayer.core.thread;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.core.system.Time;
import org.orangelogger.sys.Logger;

import java.io.File;

public class ThreadUtil {

    private ThreadUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String generateTrackThreadName(Class threadClass, Track track) {
        final File dataSource = track.getDataSource();
        final String trackName = dataSource != null ? dataSource.getName() : dataSource.toString();
        final int lengthLimit = Math.min(trackName.length(), 10);
        return threadClass.getSimpleName()+" (track=" + trackName.substring(0, lengthLimit) + ")";
    }

}
