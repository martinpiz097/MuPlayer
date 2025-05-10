package cl.estencia.labs.muplayer.util;

import cl.estencia.labs.muplayer.data.properties.support.AudioSupportInfo;

import java.io.File;
import java.nio.file.Path;

public class AudioSupportUtil {
    private final AudioSupportInfo audioSupportInfo;

    public AudioSupportUtil() {
        this.audioSupportInfo = new AudioSupportInfo();
    }

    public boolean isSupportedFile(File trackFile) {
        final String formatName = FileUtil.getFormatName(trackFile.getName());
        return audioSupportInfo.getProperty(formatName) != null;
    }

    public boolean isSupportedFile(Path track) {
        return isSupportedFile(track.toFile());
    }

    public boolean isSupportedFile(String trackPath) {
        return isSupportedFile(new File(trackPath));
    }

}
