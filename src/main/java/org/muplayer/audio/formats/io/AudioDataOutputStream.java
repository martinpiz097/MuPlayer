package org.muplayer.audio.formats.io;

import org.bytebuffer.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

public class AudioDataOutputStream extends OutputStream {

    private ByteBuffer byteBuffer;

    private boolean isClosed;

    public AudioDataOutputStream() {
        this(new ByteBuffer());
    }

    public AudioDataOutputStream(ByteBuffer byteBuffer) {
        super();
        this.byteBuffer = byteBuffer;
        isClosed = false;
    }

    ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (isClosed)
            throw new IOException("Stream closed!");
        else
            for (int i = off; i < len; i++)
                byteBuffer.add(b[i]);
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
        //byteBuffer.clear();
    }

    @Override
    public void write(int b) throws IOException {
        if (isClosed)
            throw new IOException("Stream closed!");
        else
            byteBuffer.add(b);
    }

}
