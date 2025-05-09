package org.muplayer.audio.track.io;


import org.bytebuffer.ByteBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class AudioDataInputStream extends InputStream {

    protected ByteBuffer byteBuffer;
    protected long readed;
    protected boolean isClosed;

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

    public synchronized ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        Objects.requireNonNull(b);
        if (isClosed)
            return -1;
        else {
            int available = available();
            if (available == 0)
                return -1;
            if (available < len)
                len = available;

            for (int i = 0; i < len; i++)
                b[i+off] = (byte) read();
            return len;
        }
    }

    @Override
    public synchronized long skip(long n) throws IOException {
       if (isClosed)
           return 0;
       else {
           return super.skip(n);
       }
    }

    @Override
    public synchronized int available() throws IOException {
        return (int) (byteBuffer.size()-readed);
    }

    @Override
    public synchronized void close() throws IOException {
        isClosed = true;
    }

    @Override
    public synchronized void mark(int readlimit) {
        if (!isClosed)
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
    public synchronized int read() throws IOException {
        return isClosed ? -1 : byteBuffer.get((int) readed++);
    }

}
