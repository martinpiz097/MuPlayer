package org.muplayer.thread;

import lombok.Getter;
import org.muplayer.util.CollectionUtil;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class TracksLoader {
    private final List<CompletableFuture<?>> listLoadTasks;
//    private final TracksLoaderCleaner cleaner;

    @Getter
    private static final TracksLoader instance = new TracksLoader();

    private TracksLoader() {
        this.listLoadTasks = Collections.synchronizedList(CollectionUtil.newFastArrayList());
//        this.cleaner = new TracksLoaderCleaner(listLoadTasks);
        init();
    }

    private void init() {
//        cleaner.start();
    }

    public synchronized boolean hasPendingTasks() {
        return listLoadTasks.parallelStream().anyMatch(task -> task != null && !task.isDone());
    }

    public void deleteFirstCompletedTask() {
        if (!listLoadTasks.isEmpty()) {
            listLoadTasks.removeIf(CompletableFuture::isDone);
        }
    }

    public synchronized CompletableFuture<Void> addTask(Runnable task) {
        final CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(task);
        listLoadTasks.add(completableFuture);
        new TracksLoaderCleaner(completableFuture).start();
        return completableFuture;
    }

    public synchronized <T> CompletableFuture<T> addTask(Supplier<T> task) {
        final CompletableFuture<T> completableFuture = CompletableFuture.supplyAsync(task);
        listLoadTasks.add(completableFuture);
        new TracksLoaderCleaner(completableFuture).start();
        return completableFuture;
    }

    public synchronized void removeTask(CompletableFuture<?> task) {
        listLoadTasks.remove(task);
    }

}
