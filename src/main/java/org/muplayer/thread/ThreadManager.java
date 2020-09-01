package org.muplayer.thread;

import org.muplayer.audio.util.Time;
import org.orangelogger.sys.Logger;

public class ThreadManager {

    // paso un segundo ?
    public static boolean hasOneSecond(long ti) {
        return Time.getInstance().getTime() - ti >= 1000;
    }

    public static synchronized void freezeThread(Thread thread) {
        try {
            thread.wait();
        } catch (InterruptedException e) {
            Logger.getLogger(ThreadManager.class, e.getMessage());
        }
    }

    public static void unfreezeThread(Thread thread) {
        thread.interrupt();
    }

}
