package cl.estencia.labs.muplayer.console.util;

import java.awt.image.BufferedImage;

public class ConsoleImageUtil {
    public static String createStringQuadrant(BufferedImage img, int width, int height) {
        // Caracteres: ' ', '▘', '▝', '▀', '▖', '▌', '▞', '▛', '▗', '▚', '▐', '▜', '▄', '▙', '▟', '█'
        char[] quadrants = {' ', '▘', '▝', '▀', '▖', '▌', '▞', '▛', '▗', '▚', '▐', '▜', '▄', '▙', '▟', '█'};

        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                int[] colors = new int[4];
                colors[0] = getPixel(img, x, y, width, height);         // top-left
                colors[1] = getPixel(img, x + 1, y, width, height);     // top-right
                colors[2] = getPixel(img, x, y + 1, width, height);     // bottom-left
                colors[3] = getPixel(img, x + 1, y + 1, width, height); // bottom-right

                // Encontrar colores dominantes (claro/oscuro)
                int[] bright = findDominantColors(colors);
                int fg = bright[0];
                int bg = bright[1];

                // Determinar qué cuadrantes son foreground
                int mask = 0;
                if (isCloser(colors[0], fg, bg)) mask |= 1;
                if (isCloser(colors[1], fg, bg)) mask |= 2;
                if (isCloser(colors[2], fg, bg)) mask |= 4;
                if (isCloser(colors[3], fg, bg)) mask |= 8;

                sb.append("\u001B[38;2;").append((fg >> 16) & 0xFF).append(';')
                        .append((fg >> 8) & 0xFF).append(';').append(fg & 0xFF).append("m");
                sb.append("\u001B[48;2;").append((bg >> 16) & 0xFF).append(';')
                        .append((bg >> 8) & 0xFF).append(';').append(bg & 0xFF).append("m");
                sb.append(quadrants[mask]);
            }
            sb.append("\u001B[0m\n");
        }
        return sb.toString();
    }

    public static int getPixel(BufferedImage img, int x, int y, int w, int h) {
        if (x >= w || y >= h) return 0;
        return img.getRGB(x, y) & 0xFFFFFF;
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
