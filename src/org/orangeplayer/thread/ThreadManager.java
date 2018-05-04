package org.orangeplayer.thread;

public class ThreadManager {
    public static synchronized void freezeThread(Thread t) {
        try {
            t.wait();
        } catch (InterruptedException e) {}
    }

    public static synchronized void unfreezeThread(Thread t) {
        t.interrupt();
    }

}
