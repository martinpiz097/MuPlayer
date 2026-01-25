package cl.estencia.labs.muplayer.console.util;

import java.awt.image.BufferedImage;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleChars.SEMICOLON;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleEscapeSequences.*;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.LINE_BREAK_CHAR;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.SPACE_CHAR;
import static cl.estencia.labs.muplayer.console.common.constants.KeyCodes.ESC;
import static cl.estencia.labs.muplayer.console.common.constants.KeyCodes.toChar;
import static cl.estencia.labs.muplayer.core.log.ConsolePrinter.infoLine;

public class ConsoleImageUtil {
    private static final char[] QUADRANTS = {' ', '▘', '▝', '▀', '▖', '▌', '▞', '▛', '▗', '▚', '▐', '▜', '▄', '▙', '▟', '█'};

    public static String createStringQuadrant(BufferedImage img, int terminalWidth) {
        int width = img.getWidth();
        int height = img.getHeight();

        int paddingLeft = terminalWidth < 0
                ? 0
                : Math.max(0, (terminalWidth - width) / 2);

        StringBuilder sb = new StringBuilder();
        String paddingLeftStr = String.valueOf(SPACE_CHAR).repeat(Math.max(0, terminalWidth));

        int[] colors;
        int[] bright;
        int fg, bg, mask;

        int sizeBeforeRow, sizeAfterRow;
        for (int y = 0; y < height; y += 2) {

            sizeBeforeRow = sb.length();
            for (int x = 0; x < width; x += 2) {
                colors = new int[4];
                colors[0] = getPixel(img, x, y);
                colors[1] = getPixel(img, x + 1, y);
                colors[2] = getPixel(img, x, y + 1);
                colors[3] = getPixel(img, x + 1, y + 1);

                bright = findDominantColors(colors);
                fg = bright[0];
                bg = bright[1];

                mask = 0;
                if (isCloser(colors[0], fg, bg)) mask |= 1;
                if (isCloser(colors[1], fg, bg)) mask |= 2;
                if (isCloser(colors[2], fg, bg)) mask |= 4;
                if (isCloser(colors[3], fg, bg)) mask |= 8;

                infoLine("Before: " + sb.length());
                sb.append(SETUP_FG_RGB_TRUE_COLOR).append((fg >> 16) & 0xFF).append(SEMICOLON)
                        .append((fg >> 8) & 0xFF).append(SEMICOLON).append(fg & 0xFF).append("m");
                sb.append(SETUP_BG_RGB_TRUE_COLOR).append((bg >> 16) & 0xFF).append(SEMICOLON)
                        .append((bg >> 8) & 0xFF).append(SEMICOLON).append(bg & 0xFF).append("m");
                sb.append(QUADRANTS[mask]);
                infoLine("After: " + sb.length());
            }
            sizeAfterRow = sb.length();

            if (terminalWidth > 0) {
                sb.insert(sizeBeforeRow, paddingLeftStr);
            }

            sb.append(FULL_RESET + LINE_BREAK_CHAR);
        }

        infoLine("Total: " + sb.length());
        return sb.toString();
    }

    private static int getPixel(BufferedImage img, int x, int y) {
        if (x >= img.getWidth() || y >= img.getHeight()) {
            return 0;
        }
        return img.getRGB(x, y) & 0xFFFFFF;
    }

    public static String toConsoleHalfBlocks(BufferedImage img) {
        final int width = img.getWidth();
        final int height = img.getHeight();
        final StringBuilder sb = new StringBuilder(width * height * 30);

        int top, bot, rt, gt, bt, rb, gb, bb;

        for (int y = 0; y < height; y += 2) {
            for (int x = 0; x < width; x++) {
                top = img.getRGB(x, y);
                bot = (y + 1 < height) ? img.getRGB(x, y + 1) : top;

                rt = (top >> 16) & 0xFF;
                gt = (top >> 8) & 0xFF;
                bt = top & 0xFF;

                rb = (bot >> 16) & 0xFF;
                gb = (bot >> 8) & 0xFF;
                bb = bot & 0xFF;

                sb.append(SETUP_FG_RGB_TRUE_COLOR)
                        .append(rt).append(SEMICOLON).append(gt).append(SEMICOLON).append(bt)
                        .append(";48;2;")
                        .append(rb).append(SEMICOLON).append(gb).append(SEMICOLON).append(bb)
                        .append("m▀");
            }
            sb.append(FULL_RESET + "\n");
        }
        return sb.toString();
    }

    public static String toConsoleHalfBlocks2(BufferedImage img) {
        final int width = img.getWidth();
        final int height = img.getHeight();
        final StringBuilder sb = new StringBuilder(width * height * 30);

        int top, bot, rt, gt, bt, rb, gb, bb, alphaTop, alphaBot;

        for (int y = 0; y < height; y += 2) {
            for (int x = 0; x < width; x++) {
                top = img.getRGB(x, y);
                bot = (y + 1 < height) ? img.getRGB(x, y + 1) : top;

                alphaTop = (top >> 24) & 0xFF;
                alphaBot = (bot >> 24) & 0xFF;

                // Ambos transparentes
                if (alphaTop < 128 && alphaBot < 128) {
                    sb.append(' ');
                    continue;
                }

                rt = (top >> 16) & 0xFF;
                gt = (top >> 8) & 0xFF;
                bt = top & 0xFF;

                rb = (bot >> 16) & 0xFF;
                gb = (bot >> 8) & 0xFF;
                bb = bot & 0xFF;

                if (alphaTop < 128) {
                    // Solo bottom visible
                    sb.append(SETUP_FG_RGB_TRUE_COLOR)
                            .append(rb).append(SEMICOLON).append(gb).append(SEMICOLON).append(bb)
                            .append("m" + toChar(ESC) + "[49m▄");
                } else if (alphaBot < 128) {
                    // Solo top visible
                    sb.append(SETUP_FG_RGB_TRUE_COLOR)
                            .append(rt).append(SEMICOLON).append(gt).append(SEMICOLON).append(bt)
                            .append("m" + toChar(ESC) + "[49m▀");
                } else {
                    // Ambos visibles
                    sb.append(SETUP_FG_RGB_TRUE_COLOR)
                            .append(rt).append(SEMICOLON).append(gt).append(SEMICOLON).append(bt)
                            .append(";48;2;")
                            .append(rb).append(SEMICOLON).append(gb).append(SEMICOLON).append(bb)
                            .append("m▀");
                }
            }
            sb.append(FULL_RESET + "\n");
        }
        return sb.toString();
    }

    public static int[] findDominantColors(int[] colors) {
        int brightest = colors[0], darkest = colors[0];
        int maxLum = luminance(colors[0]), minLum = maxLum;

        for (int c : colors) {
            int lum = luminance(c);
            if (lum > maxLum) { maxLum = lum; brightest = c; }
            if (lum < minLum) { minLum = lum; darkest = c; }
        }
        return new int[]{brightest, darkest};
    }

    public static int luminance(int rgb) {
        return ((rgb >> 16) & 0xFF) * 299 + ((rgb >> 8) & 0xFF) * 587 + (rgb & 0xFF) * 114;
    }

    public static boolean isCloser(int color, int fg, int bg) {
        return colorDistance(color, fg) < colorDistance(color, bg);
    }

    public static int colorDistance(int c1, int c2) {
        int dr = ((c1 >> 16) & 0xFF) - ((c2 >> 16) & 0xFF);
        int dg = ((c1 >> 8) & 0xFF) - ((c2 >> 8) & 0xFF);
        int db = (c1 & 0xFF) - (c2 & 0xFF);
        return dr * dr + dg * dg + db * db;
    }
    
}
