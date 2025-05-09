package cl.estencia.labs.muplayer.muplayer.thread;

public class TaskRunner {
    public static void execute(Runnable runnable, String threadName) {
        execute(new Thread(runnable), threadName);
    }

    public static void execute(Runnable runnable, String threadName, int priority) {
        execute(new Thread(runnable), threadName, priority);
    }

    public static void execute(Thread thread, String threadName) {
        execute(thread, threadName, Thread.NORM_PRIORITY);
    }

    public static void execute(Thread thread, String threadName, int priority) {
        thread.setName(threadName);
        thread.setPriority(priority);
        thread.start();
    }

}
