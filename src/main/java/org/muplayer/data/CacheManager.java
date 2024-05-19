package org.muplayer.data;

import java.util.Map;
import java.util.TreeMap;

public class CacheManager {
    private final Map<String, Object> mapCache;

    public CacheManager() {
        this.mapCache = new TreeMap<>();
    }

    public void saveValue(CacheVar cacheVar, Object value) {
        mapCache.put(cacheVar.name(), value);
    }

    public <T> T loadValue(CacheVar cacheVar) {
        return loadValue(cacheVar.name());
    }

    public <T> T loadValue(String cacheVarName) {
        return (T) mapCache.get(cacheVarName);
    }
}
