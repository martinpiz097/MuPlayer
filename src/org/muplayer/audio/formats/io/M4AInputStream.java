/*package org.muplayer.audio.formats.io;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.mp4.api.Track;
import org.bytebuffer.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;

public class M4AInputStream extends InputStream {
    private File inputFile;
    private RandomAccessFile randomAccess;
    private MP4Container container;
    private AudioTrack track;
    private Decoder dec;
    private SampleBuffer buff;
    private Frame frame;

    private ByteBuffer byteBuffer;
    private int bytePos;

    public M4AInputStream(File inputFile) throws IOException, UnsupportedAudioFileException {
        super();
        this.inputFile = inputFile;
        byteBuffer = new ByteBuffer();
        loadReader();
    }

    private void loadReader() throws UnsupportedAudioFileException, IOException {
        randomAccess = new RandomAccessFile(inputFile, "r");
        container = new MP4Container(randomAccess);
        final Movie movie = container.getMovie();
        final List<Track> tracks =
                movie.getTracks(AudioTrack.AudioCodec.AAC);
        if (tracks.isEmpty())
            throw new UnsupportedAudioFileException("Movie does not contain any AAC track");

        track = (AudioTrack) tracks.get(0);
        dec = new Decoder(track.getDecoderSpecificInfo());
        buff = new SampleBuffer();
    }

    public AudioFormat getFormat() {
        return new AudioFormat(track.getSampleRate(),
                track.getSampleSize(), track.getChannelCount(),
                true, true);
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

        frame = track.readNextFrame();
        dec.decodeFrame(frame.getData(), buff);
        System.out.println("AISFrameLenght: "+buff.getLength());
        byteBuffer.addFrom(buff.getData());
        // ver si es necesario hacer drain
        System.out.println("Offset: "+off);
        int read =  byteBuffer.read(b, off+bytePos, len);
        //byteBuffer.cut(len);
        bytePos+=read;
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        int available = available();
        long skipped = available < n ? available : n;
        bytePos+=skipped;
        return skipped;
    }

    @Override
    public int available() throws IOException {
        int available = (int) (randomAccess.length()-bytePos);
        return available < 0 ? 0 : (track.hasMoreFrames() ? available : 0);
    }

    @Override
    public void close() throws IOException {
        randomAccess.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        bytePos = readlimit;
    }

    @Override
    public synchronized void reset() throws IOException {
        close();
        bytePos = 0;
        try {
            loadReader();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read() throws IOException {
        return bytePos >= byteBuffer.size() ? -1 : byteBuffer.get(bytePos);
    }

}
*/