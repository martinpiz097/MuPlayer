package cl.estencia.labs.muplayer.data.reader.base.json.source;

import cl.estencia.labs.muplayer.data.reader.base.json.JsonSource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.extern.java.Log;

import java.io.File;

// Falta validar comportamiento
@Log
public class FileJsonSource<O> extends JsonSource<File, O> {
    public FileJsonSource(String sourcePath, TypeReference<O> dataType) {
        this(new File(sourcePath), dataType);
    }

    public FileJsonSource(File source, TypeReference<O> dataType) {
        super(source, dataType);
    }

    @Override
    public boolean validate() {
        try {
            boolean valid = exists();
            if (valid) {
                loadData();
            } else {
                // se crea la fuente de properties
                saveData(null);
            }
            return valid;
        } catch (Exception e) {
            log.severe("Error on PropertiesSource: " + e);
            return false;
        }
    }

    @Override
    public boolean exists() {
        return source != null && source.exists();
    }

    @Override
    public void loadData() throws Exception {
        O cacheData;
        if (!enableCache || (cacheData = getCacheData()) == null) {
            ObjectReader reader = objectMapper.readerFor(dataType);
            this.data = reader.readValue(source);
        }
        else {
            this.data = cacheData;
        }
    }

    @Override
    public void saveData(O data) throws Exception {
        if (source.length() > 0) {
            source.createNewFile();
        }
        if (data != null) {
            objectMapper.writer().writeValue(source, data);
            loadData();
        }
    }
}
