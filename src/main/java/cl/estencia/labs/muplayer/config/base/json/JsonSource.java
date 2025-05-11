package cl.estencia.labs.muplayer.config.base.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.java.Log;
import cl.estencia.labs.muplayer.cache.CacheManager;
import cl.estencia.labs.muplayer.cache.CacheVar;

@Log
public abstract class JsonSource<T, O> {
    protected final T source;
    protected final TypeReference<O> dataType;

    @Getter
    protected volatile O data;
    protected final ObjectMapper objectMapper;

    protected final CacheManager cacheManager;
    protected volatile boolean enableCache;

    public JsonSource(T source, TypeReference<O> dataType) {
        this(source, dataType, false);
    }

    public JsonSource(T source, TypeReference<O> dataType, boolean enableCache) {
        this.source = source;
        this.dataType = dataType;
        this.objectMapper = createObjectMapper();
        this.cacheManager = CacheManager.newLocalCacheManager();
        this.enableCache = enableCache;
    }

    protected ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }

    protected O getCacheData() {
        return cacheManager.loadValue(CacheVar.SOURCE_DATA);
    }

    public abstract boolean validate();
    public abstract boolean exists();
    public abstract void loadData() throws Exception;
    public abstract void saveData(O data) throws Exception;

}
