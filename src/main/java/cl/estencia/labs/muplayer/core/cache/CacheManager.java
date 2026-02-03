package cl.estencia.labs.muplayer.core.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CacheManager {
    private final Map<String, Object> mapCache;

    public static final CacheManager CACHE = new CacheManager();

    private CacheManager() {
        this.mapCache = new ConcurrentHashMap<>();
    }

    public <V> V set(CacheVar cacheVar, V value) {
        return set(cacheVar.name(), value);
    }

    public <V> V set(String cacheVarName, V value) {
        synchronized (mapCache) {
            mapCache.put(cacheVarName, value);
        }

        return value;
    }

    public <V> V get(CacheVar cacheVar) {
        return cacheVar != null ? get(cacheVar.name()) : null;
    }

    public <V> V get(String cacheVarName) {
        if (cacheVarName == null) {
            return null;
        }

        try {
            return (V) mapCache.get(cacheVarName);
        } catch (ClassCastException e) {
            log.error(e.getClass().getSimpleName() +
                    " type error when trying to obtain a cache object: " + e.getMessage());
            return null;
        }
    }

    public <V> V get(CacheVar cacheVar, Class<V> valueClass) {
        return get(cacheVar.name(), valueClass);
    }

    public <V> V get(String cacheVarName, Class<V> valueClass) {
        if (cacheVarName == null || valueClass == null) {
            return null;
        }

        try {
            return valueClass.cast(mapCache.get(cacheVarName));
        } catch (ClassCastException e) {
            log.error(e.getClass().getSimpleName() +
                    " type error when trying to obtain a cache object: " + e.getMessage());
            return null;
        }
    }

    public void remove(CacheVar cacheVar) {
        remove(cacheVar.name());
    }

    public void remove(String cacheVarName) {
        synchronized (mapCache) {
            mapCache.remove(cacheVarName);
        }
    }

    public void clear() {
        synchronized (mapCache) {
            mapCache.clear();
        }
    }

}
