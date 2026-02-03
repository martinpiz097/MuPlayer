package cl.estencia.labs.muplayer.audio.util;

import cl.estencia.labs.muplayer.audio.common.enums.SeekOption;
import cl.estencia.labs.muplayer.audio.common.enums.SupportedAudioExtension;
import cl.estencia.labs.muplayer.audio.model.PlayerStatusData;

import java.io.File;
import java.nio.file.Path;

public class AudioFileUtil {
    private static final String FORMAT_NAME_DELIMITER = "\\.";
    private static final String EMPTY = "";

    public static String getFileFormatName(File file) {
        return file != null && !file.isDirectory()
                ? getFileFormatName(file.getName())
                : null;
    }

    public static String getFileFormatName(String fileName) {
        final String[] split = fileName.trim().split(FORMAT_NAME_DELIMITER);
        return split.length > 1 ? split[split.length-1].toLowerCase().trim() : EMPTY;
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

    public static SupportedAudioExtension getAudioExtensionFromFormat(String fileFormatName) {
        if (fileFormatName == null) {
            return null;
        }

        try {
            return SupportedAudioExtension.valueOf(fileFormatName);
        } catch (Exception e) {
            return null;
        }
    }

    public static SupportedAudioExtension getAudioFileExtension(File file) {
        return getAudioExtensionFromFormat(getFileFormatName(file));
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
