package org.muplayer.audio.formats.io;

import org.bytebuffer.ByteBuffer;

import java.io.IOException;
import java.io.InputStream;

public class AudioDataInputStream extends InputStream {

    private ByteBuffer byteBuffer;
    private long readed;
    private boolean isClosed;

    public AudioDataInputStream() {
        this(new ByteBuffer());
    }

    public AudioDataInputStream(ByteBuffer byteBuffer) {
        super();
        this.byteBuffer = byteBuffer;
        readed = 0;
        isClosed = false;
    }

    public AudioDataInputStream(AudioDataOutputStream outputStream) {
        this(outputStream.getByteBuffer());
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int available = available();
        if (available < len)
            len = available;

        for (int i = 0; i < len; i++)
            b[i+off] = (byte) read();

        return len;
    }

    @Override
    public long skip(long n) throws IOException {
        readed += n;
        return readed;
    }

    @Override
    public int available() throws IOException {
        return (int) (byteBuffer.size()-readed);
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
    }

    @Override
    public synchronized void mark(int readlimit) {
        readed = readlimit;
    }

    @Override
    public synchronized void reset() throws IOException {
        mark(0);
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read() throws IOException {
        return byteBuffer.get((int) readed++);
    }

}
