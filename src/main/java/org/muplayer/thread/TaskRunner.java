package org.muplayer.thread;

public class TaskRunner {

    public static void execute(Runnable runnable, int priority) {
        Thread thread = new Thread(runnable);
        thread.setName(runnable.getClass().getSimpleName()+" "+thread.getId());
        thread.setPriority(priority);
        thread.start();
    }

    public static void execute(Runnable runnable) {
        execute(runnable, Thread.NORM_PRIORITY);
    }

}
