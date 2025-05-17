package cl.estencia.labs.muplayer.util;

import cl.estencia.labs.muplayer.audio.player.PlayerStatusData;
import cl.estencia.labs.muplayer.model.SeekOption;
import cl.estencia.labs.muplayer.model.SupportedAudioExtensions;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

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
