package org.muplayer.audio.format;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.spi.javasound.AACAudioFileReader;
import org.muplayer.audio.Track;
import org.muplayer.audio.interfaces.PlayerControls;
import org.muplayer.system.AudioUtil;
import org.orangelogger.sys.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.List;

public class M4ATrack extends Track {

    //private volatile M4AInputStream m4aStream;
    private boolean isAac;

    public M4ATrack(File dataSource) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource);
    }

    public M4ATrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath));
    }

    public M4ATrack(InputStream inputStream) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream);
    }

    public M4ATrack(File dataSource, PlayerControls player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, player);
    }

    public M4ATrack(InputStream inputStream, PlayerControls player) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream, player);
    }

    public M4ATrack(String trackPath, PlayerControls player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath, player);
    }

    @Override
    protected void loadAudioStream() {
        try {
            // Es aac
            decodeAAC();
            isAac = true;
            //new AudioTranslatorStream()
        } catch (UnsupportedAudioFileException e) {
            Logger.getLogger(this, "File not supported!").rawError();
            e.printStackTrace();
        } catch (IOException e) {
            trackStream = decodeM4A(source);
            isAac = false;
        }
        // Probar despues transformando a PCM
        //MPG4Codec codec = new MPG4Codec();
        //MP42Codec c = new MP42Codec();
        //MP4InputStream mp4 = new MP4InputStream();
    }

    private void decodeAAC() throws IOException, UnsupportedAudioFileException {
        audioReader = new AACAudioFileReader();
        trackStream = AudioUtil.instanceStream(audioReader, source);
        // Para leer progreso en segundos de vorbis(posible opcion)
        //OggInfoReader info = new OggInfoReader();
        //FlacStreamReader streamReader = new FlacStreamReader();
    }



    /*private AudioInputStream decodeM4A(File inputFile) throws IOException,
            UnsupportedAudioFileException {
        RandomAccessFile randomAccess = new RandomAccessFile(inputFile, "r");
        final MP4Container cont = new MP4Container(randomAccess);
        final List<net.sourceforge.jaad.mp4.api.Track> tracks =
                cont.getMovie().getTracks(AudioTrack.AudioCodec.AAC);
        if (tracks.isEmpty())
            throw new UnsupportedAudioFileException("Movie does not contain any AAC track");

        final AudioTrack track = (AudioTrack) tracks.get(0);
        return new M4AAudioInputStream(inputFile, new AudioDataInputStream(), track);
    }*/

    private AudioTrack getM4ATrack(Object source) throws IOException, UnsupportedAudioFileException {
        final MP4Container cont;

        if (source instanceof RandomAccessFile) {
            cont = new MP4Container((RandomAccessFile) source);
        }
        else {
            cont = new MP4Container((InputStream) source);
        }

        final Movie movie = cont.getMovie();
        final List<net.sourceforge.jaad.mp4.api.Track> tracks =
                movie.getTracks(AudioTrack.AudioCodec.AAC);

        if (tracks.isEmpty())
            throw new UnsupportedAudioFileException("Movie does not contain any AAC track");

        return (AudioTrack) tracks.get(0);
    }

    // para el caso de los m4a con contenedor quicktime
    private AudioInputStream decodeM4A(Object source) {
        try {
            final AudioTrack track;
            if (source instanceof File) {
                final RandomAccessFile randomAccess = new RandomAccessFile((File) source, "r");
                track = getM4ATrack(randomAccess);
            }
            else {
                track = getM4ATrack(source);
            }

            final Decoder dec = new Decoder(track.getDecoderSpecificInfo());

            /*m4aStream = new M4AInputStream(track, dec, randomAccess);
            AudioFormat decFormat = new AudioFormat(track.getSampleRate(),
                    track.getSampleSize(), track.getChannelCount(),
                    true, true);
            return new AudioInputStream(m4aStream, decFormat, inputFile.length());
            */
            final SampleBuffer buffer = new SampleBuffer();
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            final AudioFormat decFormat = new AudioFormat(track.getSampleRate(),
                    track.getSampleSize(), track.getChannelCount(),
                    true, true);

            //OutputStream outputStream = new AudioDataOutputStream();
            //InputStream inputStream = new AudioDataInputStream((AudioDataOutputStream) outputStream);

            Frame frame;
            while (track.hasMoreFrames()) {
                try {
                    frame = track.readNextFrame();
                    dec.decodeFrame(frame.getData(), buffer);
                    baos.write(buffer.getData());
                    //outputStream.write(buffer.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            final byte[] audioData = baos.toByteArray();
            return new AudioInputStream(new ByteArrayInputStream(audioData), decFormat, audioData.length);
            //return AudioSystem.getAudioInputStream(decFormat, AudioSystem.getAudioInputStream(inputFile));
        } catch (Exception e){
            Logger.getLogger(this, "Exception", e).error();
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected double convertSecondsToBytes(Number seconds) {
        final AudioFormat audioFormat = getAudioFormat();
        final float frameRate = audioFormat.getFrameRate();
        final int frameSize = audioFormat.getFrameSize();
        final double framesToSeek = frameRate*seconds.doubleValue();
        return framesToSeek*frameSize;
    }

    @Override
    protected double convertBytesToSeconds(Number bytes) {
        final AudioFormat audioFormat = getAudioFormat();
        return bytes.doubleValue() / audioFormat.getFrameSize() / audioFormat.getFrameRate();
    }

    @Override
    public void seek(double seconds) throws IOException {
        if (isAac) {
            mute();
            super.seek(seconds);
            unmute();
        }
        else
            super.seek(seconds);
    }

    @Override
    public void gotoSecond(double second) throws
            IOException, LineUnavailableException, UnsupportedAudioFileException {
        double progress = getProgress();
        if (second >= progress) {
            int gt = (int) Math.round(second-getProgress());
            seek(gt);
        }
        else if (second < progress) {
            pause();
            if (isAac)
                resetStream();
            else {
                trackStream.reset();
                initLine();
            }
            resumeTrack();
            secsSeeked = 0;
            seek(second);
        }
    }

    /*private long getSoundSize() {
        long size = (long) ((decodedStream.getFormat().getSampleRate()/8)*getDuration());
        System.out.println("SoundSize: "+size);
        return size;
    }*/

}
