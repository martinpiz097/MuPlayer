package cl.estencia.labs.muplayer.core.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        return get(cacheVar.name());
    }

    public <V> V get(String cacheVarName) {
        return (V) mapCache.get(cacheVarName);
    }

    public <V> V get(CacheVar cacheVar, Class<V> valueClass) {
        return get(cacheVar.name(), valueClass);
    }

    public <V> V get(String cacheVarName, Class<V> valueClass) {
        if (valueClass == null) {
            return null;
        }

        return valueClass.cast(mapCache.get(cacheVarName));
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
