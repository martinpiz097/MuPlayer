package cl.estencia.labs.muplayer.audio.util;

import cl.estencia.labs.muplayer.audio.model.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.common.enums.SeekOption;
import cl.estencia.labs.muplayer.audio.common.enums.SupportedAudioExtensions;

import java.io.File;
import java.nio.file.Path;

public class AudioFileUtil {
    private static final String FORMAT_NAME_DELIMITER = "\\.";
    private static final String EMPTY = "";

    public static String getFileFormatName(File file) {
        if (file == null || file.isDirectory()) {
            return null;
        }

        return getFileFormatName(file.getName());
    }

    public static String getFileFormatName(String fileName) {
        if (!fileName.contains(FORMAT_NAME_DELIMITER)
            || fileName.trim().endsWith(FORMAT_NAME_DELIMITER)) {
            return null;
        }

        return fileName.split(FORMAT_NAME_DELIMITER)[1].trim().toLowerCase();
    }

    public static boolean isSupportedAudioFile(File audioFile) {
        return getAudioFileExtension(audioFile) != null;
    }

    public static boolean isSupportedAudioFile(Path audioPath) {
        if (audioPath == null) {
            return false;
        }

        return isSupportedAudioFile(audioPath.toFile());
    }

    public static SupportedAudioExtensions getAudioFileExtension(File file) {
        String fileFormatName = getFileFormatName(file);
        if (fileFormatName == null) {
            return null;
        }

        try {
            return SupportedAudioExtensions.valueOf(fileFormatName);
        } catch (Exception e) {
            return null;
        }
    }

    public static int getIndexFromOption(SeekOption seekOption,
                                  PlayerStatusData playerStatusData, int tracksCount) {
        final int currentIndex = playerStatusData.getCurrentTrackIndex();

        return getIndexFromOptionAndIdx(seekOption, tracksCount, currentIndex);
    }

    public static int getIndexFromOptionAndIdx(SeekOption seekOption, int tracksCount,
                                               int selectedIndex) {
        return seekOption == SeekOption.NEXT
                ? (selectedIndex == tracksCount - 1 ? 0 : selectedIndex + 1)
                : (selectedIndex == 0 ? tracksCount - 1 : selectedIndex - 1);
    }

}
