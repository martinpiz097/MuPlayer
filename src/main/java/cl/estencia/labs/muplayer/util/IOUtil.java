package cl.estencia.labs.muplayer.util;

import org.bytebuffer.ByteBuffer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class IOUtil {

    private IOUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static final int DEFAULT_BUFFER_SIZE = 1024 * 64;

    public static boolean isEqualsBuffers(byte[] b1, byte[] b2) {
        if (b1 == b2) {
            return true;
        }
        if (b1 == null || b2 == null) {
            return false;
        }
        if (b1.length != b2.length) {
            return false;
        }
        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] getBytesFromStream(InputStream inputStream) throws IOException {
        final ByteBuffer byteBuffer = new ByteBuffer(DEFAULT_BUFFER_SIZE);

        int read;
        while ((read = inputStream.read()) != -1) {
            byteBuffer.add(read);
        }
        return byteBuffer.toArray();
    }

    public static String getAsciiStringFromStream(InputStream inputStream) throws IOException {
        final byte[] buffer = getBytesFromStream(inputStream);
        return new String(buffer, StandardCharsets.UTF_8);
    }

    public static InputStream getFileStream(File file) throws IOException {
        return Files.newInputStream(file.toPath(), StandardOpenOption.READ);
    }

    public static BufferedReader getFileBufferedReader(File file) throws IOException {
        return Files.newBufferedReader(file.toPath(), Charset.defaultCharset());
    }

    public static byte[] getResourceBytes(String resourcePath) throws IOException {
        return getBytesFromStream(getResourceAsStream(resourcePath));
    }

    public static BufferedInputStream getResourceAsBuffer(String path) {
        return new BufferedInputStream(getResourceAsStream(path), DEFAULT_BUFFER_SIZE);
    }

    public static InputStream getResourceAsStream(String path) {
        return IOUtil.class.getResourceAsStream(path);
    }

    public static InputStream getResourceAsArrayStream(String path) throws IOException {
        return new ByteArrayInputStream(getResourceBytes(path));
    }
}
