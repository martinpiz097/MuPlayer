package cl.estencia.labs.muplayer.data.reader;

import cl.estencia.labs.muplayer.data.reader.model.CompatibleAudioFormat;
import cl.estencia.labs.muplayer.data.reader.base.json.source.InternalJsonSource;
import cl.estencia.labs.muplayer.data.reader.base.json.JsonInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static cl.estencia.labs.muplayer.data.ResourceFiles.AUDIO_FORMATS_RES_PATH;

public class ResAudioSupportReader extends JsonInfo<String, List<CompatibleAudioFormat>> {

    @Getter
    private final List<CompatibleAudioFormat> listCompatibleFormats;

    private static final ResAudioSupportReader instance = new ResAudioSupportReader();

    public static ResAudioSupportReader getInstance() {
        return instance;
    }

    private ResAudioSupportReader() {
        super(new InternalJsonSource<>(
                AUDIO_FORMATS_RES_PATH,
                new TypeReference<>() {}, false));
        this.listCompatibleFormats = new ArrayList<>();
    }

    @Override
    public void loadDefaultData() {
        // load model por defecto desde codigo
    }

}
