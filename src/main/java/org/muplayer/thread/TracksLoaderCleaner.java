package org.muplayer.thread;

import lombok.extern.java.Log;

import java.util.concurrent.CompletableFuture;

@Log
public class TracksLoaderCleaner extends Thread {
    private final TracksLoader tracksLoader;
    private final CompletableFuture<?> task;

    public TracksLoaderCleaner(CompletableFuture<?> task) {
        this.tracksLoader = TracksLoader.getInstance();
        this.task = task;
        setName(getClass().getSimpleName() + " [task=" + task.hashCode() + "]");
    }

    @Override
    public void run() {
        //log.info(getName() + " started");
        while (!task.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        //log.info("track " + task.hashCode() +" completed and removed!");
        tracksLoader.removeTask(task);
        //log.info(getName() + " finished");
    }
}
