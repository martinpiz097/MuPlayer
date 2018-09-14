package org.muplayer.audio.formats.io;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class M4AAudioInputStream extends AudioInputStream {

    private File inputFile;
    private AudioFormat decFormat;
    private RandomAccessFile randomAccess;
    private MP4Container container;
    private AudioTrack track;
    private Decoder dec;
    private SampleBuffer buff;
    private Frame frame;

    private AudioDataOutputStream outputStream;
    private AudioDataInputStream inputStream;

    public M4AAudioInputStream(File inputFile, AudioDataInputStream stream, AudioTrack track) throws IOException, UnsupportedAudioFileException {
        super(stream, new AudioFormat(track.getSampleRate(),
                track.getSampleSize(), track.getChannelCount(),
                true, true), inputFile.length());
        this.inputFile = inputFile;
        outputStream = new AudioDataOutputStream(stream.getByteBuffer());
        inputStream = stream;
        new TM4AReader(inputFile, outputStream, track).start();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public AudioFormat getFormat() {
        return super.getFormat();
    }

    @Override
    public long getFrameLength() {
        return super.getFrameLength();
    }

    @Override
    public int read() throws IOException {
        return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
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
    }

    @Override
    public void mark(int readlimit) {
        super.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
    }

    @Override
    public boolean markSupported() {
        return super.markSupported();
    }

}
