package cl.estencia.labs.muplayer.console.model;

import cl.estencia.labs.muplayer.console.util.ConsoleImageUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ConsoleImage {
    private final BufferedImage originalBufferedImage;
    private final int size;

    private static final byte DEFAULT_IMAGE_SIZE = 96;

    public ConsoleImage(String imagePath) {
        this(imagePath, DEFAULT_IMAGE_SIZE);
    }

    public ConsoleImage(File imageFile) {
        this(imageFile, DEFAULT_IMAGE_SIZE);
    }

    public ConsoleImage(InputStream imageStream) {
        this(imageStream, DEFAULT_IMAGE_SIZE);
    }

    public ConsoleImage(String imagePath, int size) {
        this(new File(imagePath), size);
    }

    public ConsoleImage(File imageFile, int size) {
        this.originalBufferedImage = loadImage(imageFile);
        this.size = size;
    }

    public ConsoleImage(InputStream imageStream, int size) {
        this.originalBufferedImage = loadImage(imageStream);
        this.size = size;
    }

    private BufferedImage loadImage(File imgFile) {
        try {
            return ImageIO.read(imgFile);
        } catch (IOException e) {
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

    private BufferedImage scaleImage(BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage scaledBufferedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        drawImgAs2DGraphics(original, scaledBufferedImage, targetWidth, targetHeight);

        return scaledBufferedImage;
    }

    public String toConsoleString() {
        final int width = size;
        final int height = Math.toIntExact(Math.round(((double) width) / 3));
        final BufferedImage scaledBufferedImage = scaleImage(originalBufferedImage, width, height);

        return ConsoleImageUtil.createStringQuadrant(scaledBufferedImage, width, height);
    }

}
