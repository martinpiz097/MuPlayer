package org.muplayer.thread;

import lombok.Getter;
import lombok.Synchronized;
import org.muplayer.util.CollectionUtil;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class TracksLoader {
    private final List<CompletableFuture<?>> listLoadTasks;

    @Getter
    private static final TracksLoader instance = new TracksLoader();

    private TracksLoader() {
        this.listLoadTasks = Collections.synchronizedList(CollectionUtil.newFastList());
    }

    private void startTaskCleaner(CompletableFuture<?> task) {
        TracksLoaderCleaner cleaner = new TracksLoaderCleaner(task);
        cleaner.start();
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
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(task);
        listLoadTasks.add(completableFuture);
        startTaskCleaner(completableFuture);
        return completableFuture;
    }

    public synchronized <T> CompletableFuture<T> addTask(Supplier<T> task) {
        CompletableFuture<T> tCompletableFuture = CompletableFuture.supplyAsync(task);
        listLoadTasks.add(tCompletableFuture);
        return tCompletableFuture;
    }

    public synchronized void removeTask(CompletableFuture<?> task) {
        listLoadTasks.remove(task);
    }
}
