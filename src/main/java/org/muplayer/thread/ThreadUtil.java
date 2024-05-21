package org.muplayer.thread;

import org.muplayer.audio.track.Track;
import org.muplayer.system.Time;
import org.orangelogger.sys.Logger;

import java.io.File;

public class ThreadUtil {
    public static boolean hasOneSecond(long ti) {
        return Time.getInstance().getTime() - ti >= 1000;
    }

    public static synchronized void freezeThread(Thread thread) {
        try {
            thread.wait();
        } catch (InterruptedException e) {
            Logger.getLogger(ThreadUtil.class, e.getMessage());
        }
    }

    public static synchronized void unfreezeThread(Thread thread) {
        thread.notify();
    }

    public static String generateTrackThreadName(Class threadClass, Track track) {
        final File dataSource = track.getDataSource();
        final String trackName = dataSource != null ? dataSource.getName() : dataSource.toString();
        final int lengthLimit = Math.min(trackName.length(), 10);
        return threadClass.getSimpleName()+" (track=" + trackName.substring(0, lengthLimit) + ")";
    }

}
