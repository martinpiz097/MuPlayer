package cl.estencia.labs.muplayer.config.base.json;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class JsonInfo<T, O> {
    @Getter
    protected final JsonSource<T, O> jsonSource;

    protected JsonInfo(JsonSource<T, O> propertiesSource) {
        this.jsonSource = propertiesSource;
        try {
            if (!this.jsonSource.validate()) {
                loadDefaultData();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    protected abstract void loadDefaultData();

    public O getObject() {
        return jsonSource.getData();
    }

    public void setObject(O data) throws Exception {
        jsonSource.saveData(data);
    }
}
