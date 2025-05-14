package cl.estencia.labs.muplayer.thread;

import lombok.extern.java.Log;

import java.util.List;
import java.util.concurrent.Future;

@Log
public class TracksLoaderCleaner extends Thread {
    private final List<Future<?>> listTasks;

    public TracksLoaderCleaner(List<Future<?>> listTasks) {
        this.listTasks = listTasks;
    }

    public void deleteCompletedTasks() {
        if (!listTasks.isEmpty()) {
            listTasks.removeIf(Future::isDone);
        }
    }

    @Override
    public void run() {
        boolean canContinue = true;

        while (canContinue) {
            deleteCompletedTasks();

            while (listTasks.isEmpty()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    canContinue = false;
                    break;
                }
            }
            System.out.println("Deleting data...");
        }
    }
}
