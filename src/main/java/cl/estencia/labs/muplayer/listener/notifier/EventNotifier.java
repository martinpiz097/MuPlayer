package cl.estencia.labs.muplayer.listener.notifier;

import lombok.Getter;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Log
public abstract class EventNotifier<L, E> extends Thread {
    @Getter protected final List<L> listInternalListeners;

    @Getter protected final List<L> listUserListeners;
    protected final Deque<E> eventsQueue;

    protected final AtomicBoolean on;
    protected final AtomicBoolean notified;

    public EventNotifier() {
        this.listInternalListeners = new ArrayList<>();
        this.listUserListeners = new LinkedList<>();
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

        var internalEventsTask = CompletableFuture.runAsync(() ->
                listInternalListeners.parallelStream().forEach(
                        listener ->
                                onEventNotified(listener, event)));

        var userEventsTask = CompletableFuture.runAsync(() ->
                listUserListeners.parallelStream().forEach(
                        listener ->
                                onEventNotified(listener, event)
                ));

        return CompletableFuture.allOf(internalEventsTask, userEventsTask);
    }

    public void addInternalListener(L L) {
        synchronized (listInternalListeners) {
            listInternalListeners.add(L);
        }
    }

    public void removeInternalListener(L L) {
        synchronized (listInternalListeners) {
            listInternalListeners.remove(L);
        }
    }

    public void removeAllInternalListeners() {
        synchronized (listInternalListeners) {
            listInternalListeners.clear();
        }
    }
    
    public void addUserListener(L L) {
        synchronized (listUserListeners) {
            listUserListeners.add(L);
        }
    }

    public void removeUserListener(L L) {
        synchronized (listUserListeners) {
            listUserListeners.remove(L);
        }
    }

    public void removeAllUserListeners() {
        synchronized (listUserListeners) {
            listUserListeners.clear();
        }
    }

    public void sendEvent(E E) {
        synchronized (eventsQueue) {
            eventsQueue.add(E);
        }
        notified.set(true);
    }

    public void shutdown() {
        clearObjects();
        interrupt();
    }

    public void clearObjects() {
        listInternalListeners.clear();
        listUserListeners.clear();
        eventsQueue.clear();
    }

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
