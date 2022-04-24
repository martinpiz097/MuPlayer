package org.muplayer.thread;

public class TaskRunner {

    public static void execute(Runnable runnable, int priority) {
        final Thread thread = new Thread(runnable);
        thread.setName(runnable.getClass().getSimpleName()+" "+thread.getId());
        thread.setPriority(priority);
        thread.start();
    }

    public static void execute(Runnable runnable) {
        execute(runnable, Thread.NORM_PRIORITY);
    }

    public static void execute(Thread thread, int priority) {
        thread.setName(thread.getClass().getSimpleName()+" "+thread.getId());
        thread.setPriority(priority);
        thread.start();
    }

    public static void execute(Thread thread) {
        execute(thread, Thread.NORM_PRIORITY);
    }

}
