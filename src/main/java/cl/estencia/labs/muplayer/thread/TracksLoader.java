package cl.estencia.labs.muplayer.thread;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

public class TracksLoader {
    private final List<Future<?>> listLoadTasks;
//    private final ExecutorService executorService;
//    private final TracksLoaderCleaner cleaner;

    @Getter
    private static final TracksLoader instance = new TracksLoader();

    private TracksLoader() {
        this.listLoadTasks = Collections.synchronizedList(new LinkedList<>());
//        this.cleaner = new TracksLoaderCleaner(listLoadTasks);
//        this.executorService = Executors.newCachedThreadPool();

        init();
    }

    private void init() {
//        cleaner.start();
    }

    public synchronized boolean hasPendingTasks() {
        return listLoadTasks.parallelStream()
                .anyMatch(task -> task != null && !task.isDone());
    }

    public synchronized void addTask(Runnable task) {
        new Thread(task).start();
    }


    public synchronized void removeTask(Future<?> task) {
        listLoadTasks.remove(task);
    }

}
