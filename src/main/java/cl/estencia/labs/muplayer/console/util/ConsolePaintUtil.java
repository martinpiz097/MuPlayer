package cl.estencia.labs.muplayer.console.util;

import cl.estencia.labs.muplayer.audio.player.MusicPlayer;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.console.common.enums.ConsoleOutputMode;
import cl.estencia.labs.muplayer.console.model.ConsoleHeader;
import cl.estencia.labs.muplayer.console.runner.ConsoleRunner;
import lombok.extern.slf4j.Slf4j;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleChars.M;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleChars.SEMICOLON;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleEscapeSequences.FULL_RESET;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleEscapeSequences.SETUP_FG_RGB_TRUE_COLOR;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.*;
import static cl.estencia.labs.muplayer.console.common.enums.ConsoleOrderCode.cls;
import static cl.estencia.labs.muplayer.console.common.enums.ConsoleOutputMode.CLEAN;
import static cl.estencia.labs.muplayer.console.util.ConsolePaintUtil.GradientStyle.LIGHTEN;
import static cl.estencia.labs.muplayer.core.cache.CacheManager.CACHE;
import static cl.estencia.labs.muplayer.core.cache.CacheVar.RUNNER;
import static cl.estencia.labs.muplayer.core.log.ConsolePrinter.info;
import static cl.estencia.labs.muplayer.core.util.StringUtils.reduceString;

@Slf4j
public class ConsolePaintUtil {
    private static final int[] LIGHTEN_GREEN_GRADIENTE = new int[]{0x1A4D00, 0x2D6B08,
            0x3D8C0A, 0x4E9A12, 0x5FAD1B, 0x7CC828, 0x8CD932, 0xB8FF4A};

    private static final int[] DARKEN_GREEN_GRADIENTE = new int[] {0xB8FF4A, 0x8CD932,
            0x7CC828, 0x5FAD1B, 0x4E9A12, 0x3D8C0A, 0x2D6B08, 0x1A4D00};

    private static final int DEFAULT_VOLUME_BAR_SCALE = 20;

    private static int[] getGradientByStyle(GradientStyle gradientStyle) {
        return gradientStyle == LIGHTEN
                ? LIGHTEN_GREEN_GRADIENTE
                : DARKEN_GREEN_GRADIENTE;
    }

    public enum GradientStyle {
        LIGHTEN, DARKEN
    }

    private static String paintVolumeIcon(float systemVolume, boolean isMute) {
        int volumeInt = Math.round(systemVolume);
        if (isMute || volumeInt == 0) {
            return MUTED;
        }

        boolean headphoneOutputActive = SystemCommandExecutor.isHeadphoneOutputActive();
        if (headphoneOutputActive) {
            return HEADPHONES;
        }

        if (volumeInt >= 80) {
            return HIGH_VOLUME;
        }
        if (volumeInt >= 40) {
            return MED_VOLUME;
        }
        else {
            return LOW_VOLUME;
        }
    }

    private static String paintVolumePercent(Number volume) {
        return volume.intValue() + "%";
    }

    public static String paintVolumeStatus(float systemVoolume, boolean isMute) {
        String icon = paintVolumeIcon(systemVoolume, isMute);
        String percentage = paintVolumePercent(systemVoolume);

        return icon + percentage;
    }

    public static String paintVolumeBar(Number volume, int scale, boolean separateBars) {
        final StringBuilder sbVolume = new StringBuilder();

//        █████░░░ 60%

        // por si por error tengo volumenes mayores a 100
        final int adjustedVol = Math.min(volume.intValue(), 100);
        final int scaledVol = Math.toIntExact(Math.round(((float) (scale * adjustedVol)) / 100));

        if (separateBars) {
            for (int i = 0; i < scaledVol; i++) {
                sbVolume.append(COMPLETE_BLOCK).append(SPACE_CHAR);
            }

            for (int i = scaledVol; i < scale; i++) {
                sbVolume.append(EMPTY_BLOCK).append(SPACE_CHAR);
            }

            sbVolume.deleteCharAt(sbVolume.length() - 1);
        } else {
            for (int i = 0; i < scaledVol; i++) {
                sbVolume.append(COMPLETE_BLOCK);
            }

            for (int i = scaledVol; i < scale; i++) {
                sbVolume.append(EMPTY_BLOCK);
            }
        }

        return sbVolume.substring(0, sbVolume.length() - 1);
    }

    public static String paintVolumeBar(Number volume, int scale) {
        return paintVolumeBar(volume, scale, false);
    }

    public static String paintVolumeBar(Number volume) {
        return paintVolumeBar(volume, DEFAULT_VOLUME_BAR_SCALE);
    }

    public static String paintMusicPlayerIcons(boolean isPlaying) {
        return new StringBuilder()
                .append('(')
                .append(PREV).append(SPACE_CHAR)
                .append(isPlaying ? PAUSE : PLAY)
                .append(SPACE_CHAR).append(NEXT)
                .append(')')
                .toString();
    }

    public static String paintBatteryStatus() {
        String icon = SystemCommandExecutor.isChargerConnected() ? PLUGGED : BATTERY;
        String percentage = SystemCommandExecutor.getBatteryPercentage() + "%";

        return icon + percentage;
    }

    /**
     * Versión simplificada con colores extraídos del logo muplayer
     */
    public static String paintMuPlayerStyle(String text, GradientStyle gradientStyle) {
        final int[] gradient = getGradientByStyle(gradientStyle);
        final int textLength = text.length();
        final int maxIndex = gradient.length - 1;
        final int divisor = textLength > 1 ? textLength - 1 : 1;
        final StringBuilder sbText = new StringBuilder(textLength * 25);

        int colorIndex, color;
        for (int i = 0; i < textLength; i++) {
            colorIndex = (i * maxIndex) / divisor;
            color = gradient[colorIndex];

            sbText.append(SETUP_FG_RGB_TRUE_COLOR)
                    .append((color >> 16) & 0xFF).append(SEMICOLON)
                    .append((color >> 8) & 0xFF).append(SEMICOLON)
                    .append(color & 0xFF)
                    .append(M).append(text.charAt(i));
        }

        return sbText.append(FULL_RESET).toString();
    }

    public static String paintCurrentFolderInfo(Track currentTrack) {
        if (currentTrack == null) {
            return "";
        }

        String parentFolderName = currentTrack.getDataSource().getParentFile().getName();
        return new StringBuilder()
                .append(SPACE_CHAR)
                .append(FOLDER)
                .append(SPACE_CHAR)
                .append(reduceString(parentFolderName, 20))
                .toString();
    }

    public static String paintCurrentTrackName(Track currentTrack) {
        if (currentTrack == null) {
            return "";
        }

        return new StringBuilder()
                .append(SPACE_CHAR)
                .append(MUSICAL_NOTE)
                .append(SPACE_CHAR)
                .append(currentTrack.getTitle())
                .toString();
    }

    public static void printConsoleHeader(MusicPlayer player, ConsoleOutputMode consoleOutputMode, ConsoleRunner consoleRunner) {
        try {
            if (consoleRunner != null && consoleOutputMode == CLEAN) {
                consoleRunner.sendCommand(cls);
            }

            info(ConsoleHeader.createHeader().draw(player));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void printConsoleHeader(MusicPlayer player, ConsoleOutputMode consoleOutputMode) {
        ConsoleRunner consoleRunner = CACHE.get(RUNNER,
                ConsoleRunner.class);

        printConsoleHeader(player, consoleOutputMode, consoleRunner);
    }

}
