package cl.estencia.labs.muplayer.muplayer.data;

import java.util.Map;
import java.util.TreeMap;

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
        this.mapCache = new TreeMap<>();
    }

    public void saveValue(CacheVar cacheVar, Object value) {
        saveValue(cacheVar.name(), value);
    }

    public void saveValue(String cacheVarName, Object value) {
        mapCache.put(cacheVarName, value);
    }

    public <T> T loadValue(CacheVar cacheVar) {
        return loadValue(cacheVar.name());
    }

    public <T> T loadValue(String cacheVarName) {
        return (T) mapCache.get(cacheVarName);
    }
}
