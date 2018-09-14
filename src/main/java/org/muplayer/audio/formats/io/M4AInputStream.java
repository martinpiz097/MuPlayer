package org.muplayer.audio.formats.io;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import org.bytebuffer.ByteBuffer;

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
        System.out.println("TrackType: "+audioTrack.getType().name());
        System.out.println("DecoderInfo: "+audioTrack.getDecoderInfo());
    }

    private Frame getNextFrame() throws IOException {
        return audioTrack.readNextFrame();
    }

    private byte[] decodeNextFrame() throws IOException {
        frame = getNextFrame();
        if (frame != null) {
            decoder.decodeFrame(frame.getData(), sampleBuffer);
            return sampleBuffer.getData();
        }
        else
            return null;
    }

    private int readNextFrame() throws IOException {
        byte[] frameBytes = decodeNextFrame();
        if (frameBytes != null)
            byteBuffer.addFrom(frameBytes);
        return frameBytes == null ? -1 : frameBytes.length;
    }

    public int getFrameTime() {
        return frame == null ? 0 : (int) frame.getTime();
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
    public long skip(long seconds) throws IOException {
        /*long readCount = (long) audioTrack.seek(seconds);

        // pequeña inchorencia con readed pero que igual funciona
        if (readCount == -1 || !audioTrack.hasMoreFrames()) {
            byteBuffer.clear();
            readed = 0;
        }

        */
        double frameTime;
        double currentTime = frame == null ? 0 : frame.getTime();
        if (audioTrack.hasMoreFrames()) {
            do {
                frame = getNextFrame();
                frameTime = frame.getTime();
                System.out.print("\rFrameTime: "+frameTime);
                if ((long)(frameTime-currentTime)>=seconds)
                    break;
            } while (frame != null);
            System.out.println("\nLastFrameTime: "+frameTime);
        }
        else
            seconds = 0;
        return seconds;
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
