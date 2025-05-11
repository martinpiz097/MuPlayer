package cl.estencia.labs.muplayer.config.reader;

import cl.estencia.labs.muplayer.config.model.CompatibleAudioFormat;
import cl.estencia.labs.muplayer.config.base.json.JsonInfo;
import cl.estencia.labs.muplayer.config.base.json.source.FileJsonSource;
import cl.estencia.labs.muplayer.util.AudioFormatSupport;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.*;
import java.util.*;

import static cl.estencia.labs.muplayer.config.ResourceFiles.AUDIO_FORMATS_FILE_PATH;

public class AudioSupportReader extends JsonInfo<File, List<CompatibleAudioFormat>>
        implements AudioFormatSupport {
    private final List<CompatibleAudioFormat> listCompatibleFormats;

    public AudioSupportReader() {
        super(new FileJsonSource<>(
                AUDIO_FORMATS_FILE_PATH,
                new TypeReference<>() {}));
        this.listCompatibleFormats = new ArrayList<>();
    }

    private String getFileFormat(File file) {
        String fileName = file.getName();
        return fileName.split("\\.")[1].trim();
    }

    @Override
    public void loadDefaultData() {
        if (listCompatibleFormats.isEmpty()) {
            final ResAudioSupportReader resAudioSupportReader = ResAudioSupportReader.getInstance();
            listCompatibleFormats.addAll(resAudioSupportReader.getListCompatibleFormats());
        }
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
