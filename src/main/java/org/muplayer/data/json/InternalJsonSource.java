package org.muplayer.data.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.extern.java.Log;
import org.muplayer.data.properties.ResourceFiles;

import java.io.IOException;
import java.io.InputStream;

@Log
public class InternalJsonSource<O> extends JsonSource<String, O> {

    public InternalJsonSource(String sourcePath, TypeReference<O> dataType) {
        super(sourcePath, dataType);
    }

    @Override
    public boolean validate() {
        try {
            boolean valid = exists();
            if (valid) {
                loadData();
            }
            return valid;
        } catch (Exception e) {
            log.severe("Error on PropertiesSource: " + e);
            return false;
        }
    }

    @Override
    public boolean exists() {
        try {
            InputStream resStream = ResourceFiles.getResStream(source);
            return resStream != null && resStream.available() > 0;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void loadData() throws Exception {
        InputStream resStream = ResourceFiles.getResStream(source);
        ObjectReader reader = objectMapper.readerFor(dataType);
        this.data = reader.readValue(resStream);
    }

    @Override
    public void saveData(O data) throws Exception {
        throw new RuntimeException("Not implemented yet!");
    }
}
