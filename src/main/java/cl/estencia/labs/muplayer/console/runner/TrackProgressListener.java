package cl.estencia.labs.muplayer.console.runner;

import cl.estencia.labs.muplayer.core.cache.CacheManager;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

public class TrackProgressListener extends Thread {
    private final CacheManager globalCache;


    public TrackProgressListener() {
        this.globalCache = CacheManager.getGlobalCache();
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
//                info(();
                LockSupport.parkNanos(Duration.ofMillis(800).toNanos());
            }
        } catch (Exception e) {

        }
    }

}
