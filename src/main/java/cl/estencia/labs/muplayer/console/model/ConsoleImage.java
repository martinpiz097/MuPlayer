package cl.estencia.labs.muplayer.console.model;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static cl.estencia.labs.muplayer.console.util.ConsoleImageUtil.*;

public class ConsoleImage {
    private final BufferedImage originalBufferedImage;
    private final int width;
    private final int height;
    private final int terminalWidth;

    private static final byte DEFAULT_IMAGE_SIZE = 96;

    public ConsoleImage(String imagePath) {
        this(imagePath, DEFAULT_IMAGE_SIZE, -1);
    }

    public ConsoleImage(File imageFile) {
        this(imageFile, DEFAULT_IMAGE_SIZE, -1);
    }

    public ConsoleImage(InputStream imageStream) {
        this(imageStream, DEFAULT_IMAGE_SIZE, -1);
    }

    public ConsoleImage(String imagePath, int terminalWidth) {
        this(imagePath, DEFAULT_IMAGE_SIZE, terminalWidth);
    }

    public ConsoleImage(File imageFile, int terminalWidth) {
        this(imageFile, DEFAULT_IMAGE_SIZE, terminalWidth);
    }

    public ConsoleImage(InputStream imageStream, int terminalWidth) {
        this(imageStream, DEFAULT_IMAGE_SIZE, terminalWidth);
    }

    public ConsoleImage(String imagePath, int size, int terminalWidth) {
        this(new File(imagePath), size, terminalWidth);
    }

    public ConsoleImage(File imageFile, int size, int terminalWidth) {
        this.originalBufferedImage = loadImage(imageFile);
        this.width = calculateWidth(size);
        this.height = calculateHeight(size);
        this.terminalWidth = terminalWidth;
    }

    public ConsoleImage(InputStream imageStream, int size, int terminalWidth) {
        this.originalBufferedImage = loadImage(imageStream);
        this.width = calculateWidth(size);
        this.height = calculateHeight(size);
        this.terminalWidth = terminalWidth;
    }

    public ConsoleImage(String imagePath, int width, int height, int terminalWidth) {
        this(new File(imagePath), width, height, terminalWidth);
    }

    public ConsoleImage(File imageFile, int width, int height, int terminalWidth) {
        this.originalBufferedImage = loadImage(imageFile);
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
        this.terminalWidth = terminalWidth;
    }

    public ConsoleImage(InputStream imageStream, int width, int height, int terminalWidth) {
        this.originalBufferedImage = loadImage(imageStream);
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
        this.terminalWidth = terminalWidth;
    }

    private BufferedImage loadImage(File imgFile) {
        try {
            if (!imgFile.exists()) {
                return null;
            }

            return ImageIO.read(imgFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private BufferedImage loadImage(InputStream imgStream) {
        try {
            return ImageIO.read(imgStream);
        } catch (IOException e) {
            return null;
        }
    }

    private void drawImgAs2DGraphics(BufferedImage originImage, BufferedImage destImage, int targetWidth, int targetHeight) {
        final Graphics2D graphics2D = destImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(originImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
    }

    private BufferedImage scaleImage(BufferedImage original) {
        int scaledWidth = width > 0 ? width : original.getWidth();
        int scaledHeight = height > 0 ? height : original.getHeight();
        BufferedImage scaledBufferedImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);

        drawImgAs2DGraphics(original, scaledBufferedImage, scaledWidth, scaledHeight);
        return scaledBufferedImage;
    }

    private int calculateWidth(int size) {
        return size < 1 ? 0 : size;
    }

    private int calculateHeight(int size) {
        if (size < 1) {
            return 0;
        }

        return Math.toIntExact(Math.round(((double) size) / 3));
    }

    public String drawString() {
        final BufferedImage scaledBufferedImage = scaleImage(originalBufferedImage);

//        infoLine("Terminal Width: " + terminalWidth);
//        infoLine("Width: " + scaledBufferedImage.getWidth());

        return toConsoleHalfBlocks2
                (scaledBufferedImage);
    }

}
