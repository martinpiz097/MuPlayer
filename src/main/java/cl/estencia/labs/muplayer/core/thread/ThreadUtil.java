package cl.estencia.labs.muplayer.core.thread;

import cl.estencia.labs.muplayer.audio.track.Track;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.LockSupport;

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

    public static void sleepInSeconds(int seconds) {
        LockSupport.parkNanos(Duration.ofSeconds(seconds).toNanos());
    }

    public static void sleepInMillis(long millis) {
        LockSupport.parkNanos(Duration.ofMillis(millis).toNanos());
    }

    public static void sleepInNanos(long nanos) {
        LockSupport.parkNanos(nanos);
    }

    public static <V> V getTaskValueOrNull(Future<V> task) {
        try {
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

}
