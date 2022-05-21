package org.muplayer.thread;

import org.muplayer.system.Time;
import org.orangelogger.sys.Logger;

public class ThreadUtil {

    // paso un segundo ?
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

}
