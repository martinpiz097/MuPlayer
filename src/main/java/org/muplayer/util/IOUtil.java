package org.muplayer.util;

import org.bytebuffer.ByteBuffer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class IOUtil {
    public static byte[] getBytesFromStream(InputStream inputStream) throws IOException {
        final ByteBuffer byteBuffer = new ByteBuffer();

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

    public static byte[] getBytesFromPath(String path) throws IOException {
        return Files.readAllBytes(new File(path).toPath());
    }

    public static byte[] getBytesFromRes(String path) throws IOException {
        return getBytesFromStream(DataUtil.getResourceAsStream(path));
    }

    public static InputStream getArrayStreamFromPath(String path) throws IOException {
        return new ByteArrayInputStream(getBytesFromPath(path));
    }

    public static InputStream getArrayStreamFromRes(String path) throws IOException {
        return new ByteArrayInputStream(getBytesFromRes(path));
    }
}
