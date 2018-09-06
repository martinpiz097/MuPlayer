package org.muplayer.thread;

import org.muplayer.audio.util.Time;

public class ThreadManager {

    // paso un segundo ?
    public static boolean hasOneSecond(long ti) {
        //return System.currentTimeMillis() - ti >= 1000;
        return Time.getInstance().getTime() - ti >= 1000;
    }

    public static synchronized void freezeThread(Thread t) {
        try {
            t.wait();
        } catch (InterruptedException e) {}
    }

    public static synchronized void unfreezeThread(Thread t) {
        t.interrupt();
    }

}
