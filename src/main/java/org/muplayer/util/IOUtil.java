package org.muplayer.util;

import org.bytebuffer.ByteBuffer;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class IOUtil {
    public static boolean isEqualsBuffers(byte[] b1, byte[] b2) {
        if (b1 == null && b2 == null)
            return true;
        if (b1 == null || b2 == null)
            return false;
        if (b1.length != b2.length)
            return false;

        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i])
                return false;
        }
        return true;
    }

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

    public static InputStream getInputStream(File file) throws IOException {
        return Files.newInputStream(file.toPath(), StandardOpenOption.READ);
    }

    public static BufferedReader getBufferedReader(File file) throws IOException {
        return Files.newBufferedReader(file.toPath(), Charset.defaultCharset());
    }

    public static InputStream getArrayStreamFromRes(String path) throws IOException {
        return new ByteArrayInputStream(getBytesFromRes(path));
    }
}
