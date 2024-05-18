package org.muplayer.thread;

import lombok.Getter;
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

    public boolean hasPendingTasks() {
        return listLoadTasks.parallelStream().anyMatch(task -> !task.isDone());
    }

    public void deleteFirstCompletedTask() {
        if (!listLoadTasks.isEmpty()) {
            listLoadTasks.removeIf(CompletableFuture::isDone);
        }
    }

    public CompletableFuture<Void> addTask(Runnable task) {
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(task);
        listLoadTasks.add(completableFuture);
        return completableFuture;
    }

    public <T> CompletableFuture<T> addTask(Supplier<T> task) {
        CompletableFuture<T> tCompletableFuture = CompletableFuture.supplyAsync(task);
        listLoadTasks.add(tCompletableFuture);
        return tCompletableFuture;
    }

    public void removeTask(CompletableFuture<?> task) {
        listLoadTasks.remove(task);
    }
}
