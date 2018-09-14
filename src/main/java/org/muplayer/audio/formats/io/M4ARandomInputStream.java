/*package org.muplayer.audio.formats.io;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.mp4.api.Track;
import org.aucom.sound.AudioQuality;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.List;

public class M4ARandomInputStream extends AudioInputStream {
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

    public M4ARandomInputStream(File inputFile) throws IOException, UnsupportedAudioFileException {
        super(new AudioDataInputStream(new AudioDataOutputStream()),
                AudioQuality.HIGH, inputFile.length());
        this.inputFile = inputFile;
        outputStream = new AudioDataOutputStream();
        inputStream = new AudioDataInputStream(outputStream);
        loadReader();
        // la idea es crear un inputstream alternativo el cual sea un reemplazo del
        // bytearrayinputstream y que a medida que el ais lea este cargue datos del buffer de m4a
    }

    private void loadReader() throws IOException, UnsupportedAudioFileException {
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

        decFormat = new AudioFormat(track.getSampleRate(),
                track.getSampleSize(), track.getChannelCount(),
                true, true);
    }

    @Override
    public AudioFormat getFormat() {
        return decFormat;
    }

    @Override
    public long getFrameLength() {
        return decFormat.getFrameSize();
    }

    @Override
    public int read() throws IOException {
        return track.hasMoreFrames() ? 0 : -1;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (track.hasMoreFrames() || inputStream.available() > 0) {
            if (track.hasMoreFrames()) {
                frame = track.readNextFrame();
                dec.decodeFrame(frame.getData(), buff);
                outputStream.write(buff.getData());
            }
            return inputStream.read(b);
        }
        else
            return -1;
    }

    @Override
    public long skip(long n) throws IOException {
        System.out.println("Skip!");
        return 0;
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
        inputStream.close();
    }

    @Override
    public void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

}
*/