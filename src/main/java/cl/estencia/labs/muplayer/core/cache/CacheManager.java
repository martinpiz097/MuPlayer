package cl.estencia.labs.muplayer.core.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {
    private final Map<String, Object> mapCache;

    public static final CacheManager GLOBAL_CACHE = new CacheManager();

    private CacheManager() {
        this.mapCache = new ConcurrentHashMap<>();
    }

    public <V> V saveValue(CacheVar cacheVar, V value) {
        return saveValue(cacheVar.name(), value);
    }

    public <V> V saveValue(String cacheVarName, V value) {
        synchronized (mapCache) {
            mapCache.put(cacheVarName, value);
        }

        return value;
    }

    public <V> V loadValue(CacheVar cacheVar) {
        return loadValue(cacheVar.name());
    }

    public <V> V loadValue(String cacheVarName) {
        return (V) mapCache.get(cacheVarName);
    }

    public <V> V loadValue(CacheVar cacheVar, Class<V> valueClass) {
        return loadValue(cacheVar.name(), valueClass);
    }

    public <V> V loadValue(String cacheVarName, Class<V> valueClass) {
        if (valueClass == null) {
            return null;
        }

        return valueClass.cast(mapCache.get(cacheVarName));
    }

    public void removeValue(CacheVar cacheVar) {
        removeValue(cacheVar.name());
    }

    public void removeValue(String cacheVarName) {
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
