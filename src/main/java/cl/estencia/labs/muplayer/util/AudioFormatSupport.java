package cl.estencia.labs.muplayer.util;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.config.model.CompatibleAudioFormat;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public interface AudioFormatSupport {
    default boolean isSupportedFile(Path track) {
        return isSupportedFile(track.toFile());
    }
    default boolean isSupportedFile(String trackPath) {
        return isSupportedFile(new File(trackPath));
    }
    boolean isSupportedFile(File trackFile);
    List<CompatibleAudioFormat> getSupportedFormats();
    List<String> getSupportedFileFormats();
    List<String> getSupportedFormatClassNames();
    default List<Class<? extends Track>> getSupportedFormatClasses() {
        return getSupportedFormatClassNames()
                .stream().map(className -> {
                    try {
                        return (Class<? extends Track>) Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        System.err.println(e.getMessage());
                        return (Class<? extends Track>) null;
                    }
                }).filter(Objects::nonNull)
                .toList();
    }
}
