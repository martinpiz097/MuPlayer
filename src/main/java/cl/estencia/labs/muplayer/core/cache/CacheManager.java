package cl.estencia.labs.muplayer.core.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {
    private final Map<String, Object> mapCache;

    private static final CacheManager globalCacheManager = new CacheManager();

    public static CacheManager getGlobalCache() {
        return globalCacheManager;
    }

    public static CacheManager newLocalCacheManager() {
        return new CacheManager();
    }

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
        return valueClass.cast(mapCache.get(cacheVarName));
    }

}
