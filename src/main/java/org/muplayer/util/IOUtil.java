package org.muplayer.util;

import org.bytebuffer.ByteBuffer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.muplayer.properties.PropertiesFilesInfo.INFO_FILE_PATH;

public class IOUtil {
    public static byte[] getBytesFromStream(InputStream inputStream) throws IOException {
        final ByteBuffer byteBuffer = new ByteBuffer();

        int read;
        while ((read = inputStream.read()) != -1) {
            byteBuffer.add(read);
        }

        return byteBuffer.toArray();
    }

    public static byte[] getBytesFromPath(String path) throws IOException {
        return Files.readAllBytes(new File(path).toPath());
    }

    public static byte[] getBytesFromRes(String path) throws IOException {
        return getBytesFromStream(IOUtil.class.getResourceAsStream(INFO_FILE_PATH));
    }

    public static InputStream getArrayStreamFromPath(String path) throws IOException {
        return new ByteArrayInputStream(getBytesFromPath(path));
    }

    public static InputStream getArrayStreamFromRes(String path) throws IOException {
        return new ByteArrayInputStream(getBytesFromRes(path));
    }
}
