package cl.estencia.labs.muplayer.audio.util;

import cl.estencia.labs.muplayer.audio.player.PlayerStatusData;
import cl.estencia.labs.muplayer.core.common.enums.SeekOption;
import cl.estencia.labs.muplayer.core.common.enums.SupportedAudioExtensions;
import cl.estencia.labs.muplayer.core.util.FileUtil;

import java.io.File;
import java.nio.file.Path;

public class AudioFormatUtil {
    public boolean hasAudioFormatExtension(Path audioFilePath) {
        return audioFilePath != null && hasAudioFormatExtension(audioFilePath.toFile());
    }

    public boolean hasAudioFormatExtension(File audioFile) {
        if (audioFile == null || !audioFile.exists() || audioFile.isDirectory()) {
            return false;
        }

        String fileFormatName = FileUtil.getFileFormatName(audioFile);

        try {
            SupportedAudioExtensions.valueOf(fileFormatName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int getIndexFromOption(SeekOption seekOption,
                                  PlayerStatusData playerStatusData, int tracksCount) {
        final int currentIndex = playerStatusData.getCurrentTrackIndex();

        int newIndex;
        if (seekOption == SeekOption.NEXT) {
            newIndex = currentIndex == tracksCount - 1 ? 0 : currentIndex + 1;
        } else {
            newIndex = currentIndex == 0 ? tracksCount - 1 : currentIndex - 1;
        }

        return newIndex;
    }

}
