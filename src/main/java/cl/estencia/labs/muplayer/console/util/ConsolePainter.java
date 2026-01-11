package cl.estencia.labs.muplayer.console.util;

import lombok.extern.slf4j.Slf4j;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleChars.M;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleChars.SEMICOLON;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleEscapeSequences.FULL_RESET;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleEscapeSequences.SETUP_FG_RGB_TRUE_COLOR;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.*;
import static cl.estencia.labs.muplayer.console.util.ConsolePainter.GradientStyle.LIGHTEN;

@Slf4j
public class ConsolePainter {

    private static final int[] LIGHTEN_GREEN_GRADIENTE = new int[]{0x1A4D00, 0x2D6B08,
            0x3D8C0A, 0x4E9A12, 0x5FAD1B, 0x7CC828, 0x8CD932, 0xB8FF4A};

    private static final int[] DARKEN_GREEN_GRADIENTE = new int[] {0xB8FF4A, 0x8CD932,
            0x7CC828, 0x5FAD1B, 0x4E9A12, 0x3D8C0A, 0x2D6B08, 0x1A4D00};

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

    public static String paintVolumeBar(Number volume, int scale) {
        final StringBuilder sbVolume = new StringBuilder();

//        █████░░░ 60%

        // por si por error tengo volumenes mayores a 100
        final int adjustedVol = Math.min(volume.intValue(), 100);
        final int scaledVol = Math.toIntExact(Math.round(((float) (scale * adjustedVol)) / 100));

        for (int i = 0; i < scaledVol; i++) {
            sbVolume.append(COMPLETE_BLOCK);
        }

        for (int i = scaledVol; i < scale; i++) {
            sbVolume.append(EMPTY_BLOCK);
        }

        return sbVolume.toString();
    }

    public static String paintMusicPlayerIcons(boolean isPlaying) {
        return new StringBuilder()
                .append('(')
                .append(PREV).append(SPACE)
                .append(isPlaying ? PAUSE : PLAY)
                .append(SPACE).append(NEXT)
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


}
