package cl.estencia.labs.muplayer.core.util;

import cl.estencia.labs.muplayer.audio.player.PlayerStatusData;
import cl.estencia.labs.muplayer.core.common.enums.SeekOption;
import cl.estencia.labs.muplayer.core.common.enums.SupportedAudioExtensions;

import java.io.File;
import java.io.IOException;
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
        return split.length > 0 ? split[split.length-1].toLowerCase().trim() : EMPTY;
    }

    public static boolean hasAudioFormatExtension(File audioFile) {
        return getAudioFileExtension(audioFile) != null;
    }

    public static boolean hasAudioFormatExtension(Path audioPath) {
        if (audioPath == null) {
            return false;
        }

        return hasAudioFormatExtension(audioPath.toFile());
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

    public static String getPath(File file) {
        try {
            return file != null ? file.getCanonicalPath() : null;
        } catch (IOException e) {
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
