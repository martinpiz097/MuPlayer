package cl.estencia.labs.muplayer.data.reader;

import cl.estencia.labs.muplayer.data.reader.model.CompatibleAudioFormat;
import cl.estencia.labs.muplayer.data.reader.base.json.JsonInfo;
import cl.estencia.labs.muplayer.data.reader.base.json.JsonSource;
import cl.estencia.labs.muplayer.data.reader.base.json.source.FileJsonSource;
import cl.estencia.labs.muplayer.model.AudioSupport;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static cl.estencia.labs.muplayer.data.ResourceFiles.AUDIO_FORMATS_FILE_PATH;

public class AudioSupportReader extends JsonInfo<File, List<CompatibleAudioFormat>> {

    private final List<CompatibleAudioFormat> listCompatibleFormats;

    public AudioSupportReader(JsonSource<String,
            List<CompatibleAudioFormat>> propertiesSource) {
        super(new FileJsonSource<>(
                AUDIO_FORMATS_FILE_PATH,
                new TypeReference<>() {}));
        this.listCompatibleFormats = new ArrayList<>();
    }

    @Override
    public void loadDefaultData() {
        if (properties.isEmpty()) {
            final ResAudioSupportReader resAudioSupportReader = ResAudioSupportReader.getInstance();
            resAudioSupportReader.getPropertyNames().forEach(
                    name -> {
                        try {
                            setProperty(name, resAudioSupportReader.getProperty(name));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
            try {
                propertiesSource.saveData(properties, DEFAULT_COMMENT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getProperty(String key) {
        return super.getProperty(key.startsWith(KEY_PREFFIX) ? key : KEY_PREFFIX.concat(key));
    }

    public Set<String> getAudioExtensions() {
        return getPropertyNames()
                .parallelStream()
                .map(prop->prop.substring(KEY_PREFFIX.length()))
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setProperty(AudioSupport audioSupport) throws Exception {
        setProperty(KEY_PREFFIX.concat(audioSupport.getExtension()),
                audioSupport.getAudioClass().getName());
    }

    @Override
    public void setProperty(String key, String value) throws Exception {
        super.setProperty(key.startsWith(KEY_PREFFIX) ? key : KEY_PREFFIX.concat(key), value);
    }
}
