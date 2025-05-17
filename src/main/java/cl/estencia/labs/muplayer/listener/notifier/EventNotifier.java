package cl.estencia.labs.muplayer.listener.notifier;

import cl.estencia.labs.muplayer.interfaces.Listenable;
import cl.estencia.labs.muplayer.listener.TrackStateListener;
import lombok.Getter;
import lombok.extern.java.Log;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Log
public abstract class EventNotifier<L, E> extends Thread implements Listenable<L, E> {
    @Getter protected final List<L> listListeners;
    protected final Deque<E> eventsQueue;

    protected final AtomicBoolean on;
    protected final AtomicBoolean notified;

    public EventNotifier() {
        this.listListeners = new ArrayList<>();
        this.eventsQueue = new LinkedList<>();

        this.notified = new AtomicBoolean(false);
        this.on = new AtomicBoolean(false);
    }

    protected void waitForNotification() throws InterruptedException {
        while (!notified.get()) {
            Thread.sleep(10);
        }
    }

    protected abstract void onEventNotified(L listener, E event);

    protected CompletableFuture<Void> notifyListeners(E event) {
        if (event == null) {
            return CompletableFuture.runAsync(() -> {});
        }

        return CompletableFuture.runAsync(() ->
                listListeners.parallelStream().forEach(
                        listener ->
                                onEventNotified(listener, event)));
    }

    @Override
    public void addListener(L listener) {
        synchronized (listListeners) {
            listListeners.add(listener);
        }
    }

    @Override
    public List<L> getAllListeners() {
        return listListeners;
    }

    @Override
    public void removeListener(L listener) {
        synchronized (listListeners) {
            listListeners.remove(listener);
        }
    }

    @Override
    public void removeAllListeners() {
        synchronized (listListeners) {
            listListeners.clear();
        }
    }

    @Override
    public void sendEvent(E event) {
        synchronized (eventsQueue) {
            eventsQueue.add(event);
        }
        notified.set(true);
    }

    public void shutdown() {
        interrupt();
        clearObjects();
    }

    public void clearObjects() {
        listListeners.clear();
        eventsQueue.clear();
        clearCustomObjects();
    }

    public abstract void clearCustomObjects();

    @Override
    public void run() {
        on.set(true);

        while (on.get()) {
            try {
                waitForNotification();
                while (!eventsQueue.isEmpty()) {
                    notifyListeners(eventsQueue.pollFirst());
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                on.set(false);
            }
        }

    }
}
