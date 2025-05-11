package cl.estencia.labs.muplayer.config.base.json;

import lombok.Getter;

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
            e.printStackTrace();
        }
    }

    protected abstract void loadDefaultData();

    public O getObject() throws Exception {
        return jsonSource.getData();
    }

    public void setObject(O data) throws Exception {
        jsonSource.saveData(data);
    }
}
