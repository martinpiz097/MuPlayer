package cl.estencia.labs.muplayer.console.util;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleCommonChars.M;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleCommonChars.SEMICOLON;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleEscapeSequences.FULL_RESET;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleEscapeSequences.SETUP_FG_RGB_TRUE_COLOR;
import static cl.estencia.labs.muplayer.console.util.ConsoleTextPainter.GradientStyle.LIGHTEN;

public class ConsoleTextPainter {
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