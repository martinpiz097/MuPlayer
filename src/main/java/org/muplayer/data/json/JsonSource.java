package org.muplayer.data.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.java.Log;

@Log
public abstract class JsonSource<T, O> {
    protected final T source;
    protected final TypeReference<O> dataType;

    @Getter
    protected volatile O data;
    protected final ObjectMapper objectMapper;

    public JsonSource(T source, TypeReference<O> dataType) {
        this.source = source;
        this.dataType = dataType;
        this.objectMapper = createObjectMapper();
    }

    protected ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }

    public abstract boolean validate();
    public abstract boolean exists();
    public abstract void loadData() throws Exception;
    public abstract void saveData(O data) throws Exception;

}
