package org.muplayer.audio.formats.io;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import org.bytebuffer.ByteBuffer;
import org.muplayer.system.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;

public class M4AInputStream extends AudioDataInputStream {
    private AudioTrack audioTrack;
    private Decoder decoder;
    private RandomAccessFile randomAccessFile;

    private Frame frame;
    private SampleBuffer sampleBuffer;

    public M4AInputStream(AudioTrack audioTrack, Decoder decoder, RandomAccessFile randomAccessFile) {
        this(new ByteBuffer(), audioTrack, decoder, randomAccessFile);
    }

    public M4AInputStream(ByteBuffer byteBuffer, AudioTrack audioTrack,
                          Decoder decoder, RandomAccessFile randomAccessFile) {
        super(byteBuffer);
        this.audioTrack = audioTrack;
        this.decoder = decoder;
        this.randomAccessFile = randomAccessFile;
        sampleBuffer = new SampleBuffer();
    }

    private byte[] decodeNextFrame() throws IOException {
        if (audioTrack.hasMoreFrames()) {
            frame = audioTrack.readNextFrame();
            Logger.getLogger(this, "EncodedLenght: "+frame.getData().length).rawWarning();
            decoder.decodeFrame(frame.getData(), sampleBuffer);
            Logger.getLogger(this, "DecodedLenght: "+sampleBuffer.getData().length).rawWarning();
            Logger.getLogger(this, "---------------------------").rawWarning();
            return sampleBuffer.getData();
        }
        else
            return null;
    }

    private void readNextFrame() throws IOException {
        byte[] nextFrame = decodeNextFrame();
        if (nextFrame != null)
            byteBuffer.addFrom(nextFrame);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        readNextFrame();
        readNextFrame();
        return super.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return super.skip(n);
    }

    @Override
    public int available() throws IOException {
        return super.available();
    }

    @Override
    public void close() throws IOException {
        super.close();
        randomAccessFile.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        super.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();
    }

    @Override
    public boolean markSupported() {
        return super.markSupported();
    }

    @Override
    public int read() throws IOException {
        return super.read();
    }
}
