package cl.estencia.labs.muplayer.audio.track.listener;

import lombok.Getter;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Log
public class TrackNotifier extends Thread {
    private final List<TrackStateListener> listInternalListeners;
    @Getter
    private final List<TrackStateListener> listUserListeners;
    private final Deque<TrackEvent> eventsQueue;

    private final AtomicBoolean on;
    private final AtomicBoolean notified;

    public TrackNotifier() {
        this.listInternalListeners = new ArrayList<>();
        this.listUserListeners = new LinkedList<>();
        this.eventsQueue = new LinkedList<>();

        this.notified = new AtomicBoolean(false);
        this.on = new AtomicBoolean(false);
    }

    private void waitForNotification() throws InterruptedException {
        while (!notified.get()) {
            Thread.sleep(10);
        }
    }

    public void addUserListener(TrackStateListener trackStateListener) {
        synchronized (listUserListeners) {
            listUserListeners.add(trackStateListener);
        }
    }

    public void removeUserListener(TrackStateListener trackStateListener) {
        synchronized (listUserListeners) {
            listUserListeners.remove(trackStateListener);
        }

    }

    private CompletableFuture<Void> notifyListeners(TrackEvent trackEvent) {
        if (trackEvent == null) {
            return CompletableFuture.runAsync(() -> {});
        }

        log.warning("New event: " + trackEvent);
        var internalEventsTask = CompletableFuture.runAsync(() ->
                listInternalListeners.parallelStream().forEach(
                        listener -> listener.onStateChange(trackEvent)));

        var userEventsTask = CompletableFuture.runAsync(() ->
                listUserListeners.parallelStream().forEach(
                        listener -> listener.onStateChange(trackEvent)
                ));

        return CompletableFuture.allOf(internalEventsTask, userEventsTask);
    }

    public void sendEvent(TrackEvent trackEvent) {
        synchronized (eventsQueue) {
            eventsQueue.add(trackEvent);
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
