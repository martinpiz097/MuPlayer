/*package org.muplayer.thread;

import java.util.Iterator;
import java.util.LinkedList;

public class ThreadFinalizer extends Thread {

    private final LinkedList<Thread> listThreads;

    public ThreadFinalizer() {
        this.listThreads = new LinkedList<>();
    }

    public void addThread(Thread t) {
        listThreads.add(t);
    }

    @Override
    public void run() {
        Iterator<Thread> it;
        Thread t;
        while (true) {
            while (!listThreads.isEmpty()) {
                while ((it = listThreads.iterator()).hasNext()) {
                    t = it.next();
                    while (t.isAlive()){}
                    it.remove();
                }
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
*/
