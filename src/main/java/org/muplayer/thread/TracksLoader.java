package org.muplayer.thread;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class TracksLoader {
    private final List<CompletableFuture<?>> listLoadTasks;

    @Getter
    private static final TracksLoader instance = new TracksLoader();

    private TracksLoader() {
        this.listLoadTasks = new LinkedList<>();
    }

    public boolean areAllCompleted() {
        return listLoadTasks.parallelStream().allMatch(CompletableFuture::isDone);
    }

    public void addTask(Runnable task) {
        listLoadTasks.add(CompletableFuture.runAsync(task));
    }

    public <T> CompletableFuture<T> addTask(Supplier<T> task) {
        CompletableFuture<T> tCompletableFuture = CompletableFuture.supplyAsync(task);
        listLoadTasks.add(tCompletableFuture);
        return tCompletableFuture;
    }
}
