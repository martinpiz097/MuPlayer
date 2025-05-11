package cl.estencia.labs.muplayer.config.reader;

import cl.estencia.labs.muplayer.config.model.CompatibleAudioFormat;
import cl.estencia.labs.muplayer.config.base.json.source.InternalJsonSource;
import cl.estencia.labs.muplayer.config.base.json.JsonInfo;
import cl.estencia.labs.muplayer.model.AudioSupport;
import cl.estencia.labs.muplayer.util.AudioFormatSupport;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static cl.estencia.labs.muplayer.config.ResourceFiles.AUDIO_FORMATS_RES_PATH;

public class ResAudioSupportReader extends JsonInfo<String, List<CompatibleAudioFormat>>
        implements AudioFormatSupport {

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

    private String getFileFormat(File file) {
        String fileName = file.getName();
        return fileName.split("\\.")[1].trim();
    }

    @Override
    public void loadDefaultData() {
    }

    @Override
    public boolean isSupportedFile(File trackFile) {
        return listCompatibleFormats.parallelStream()
                .anyMatch(
                        compatibleAudioFormat ->
                                compatibleAudioFormat.formatName().trim()
                                        .equals(getFileFormat(trackFile)));
    }

    @Override
    public List<CompatibleAudioFormat> getSupportedFormats() {
        return new ArrayList<>(listCompatibleFormats);
    }

    @Override
    public List<String> getSupportedFileFormats() {
        return listCompatibleFormats.parallelStream()
                .map(CompatibleAudioFormat::formatName)
                .toList();
    }

    @Override
    public List<String> getSupportedFormatClassNames() {
        return listCompatibleFormats.parallelStream()
                .map(CompatibleAudioFormat::formatClass)
                .toList();
    }

}
