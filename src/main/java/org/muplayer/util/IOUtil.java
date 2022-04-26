package org.muplayer.util;

import org.bytebuffer.ByteBuffer;

import java.io.IOException;
import java.io.InputStream;

public class IOUtil {
    public static byte[] getBytesFromStream(InputStream inputStream) throws IOException {
        final ByteBuffer byteBuffer = new ByteBuffer();

        int read;
        while ((read = inputStream.read()) != -1) {
            byteBuffer.add(read);
        }

        return byteBuffer.toArray();
    }
}
