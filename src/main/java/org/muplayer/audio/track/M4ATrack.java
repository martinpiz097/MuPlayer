package org.muplayer.audio.track;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.spi.javasound.AACAudioFileReader;
import org.muplayer.audio.player.Player;
import org.muplayer.util.AudioUtil;
import org.orangelogger.sys.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.List;

public class M4ATrack extends Track {
    private boolean isAac;

    public M4ATrack(File dataSource) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource);
    }

    public M4ATrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath);
    }

    public M4ATrack(File dataSource, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, player);
    }

    public M4ATrack(String trackPath, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath, player);
    }

    @Override
    protected void loadAudioStream() {
        try {
            // Es aac
            decodeAAC();
            isAac = true;
        } catch (UnsupportedAudioFileException e) {
            Logger.getLogger(this, "File not supported!").rawError();
            e.printStackTrace();
        } catch (IOException e) {
            trackIO = new TrackIO();
            trackIO.setDecodedStream(decodeM4A(dataSource));
            isAac = false;
        }
        // Probar despues transformando a PCM
        //MPG4Codec codec = new MPG4Codec();
        //MP42Codec c = new MP42Codec();
        //MP4InputStream mp4 = new MP4InputStream();
    }

    private void decodeAAC() throws IOException, UnsupportedAudioFileException {
        trackIO = new TrackIO();
        trackIO.setAudioReader(new AACAudioFileReader());
        trackIO.setDecodedStream(AudioUtil.instanceStream(trackIO.getAudioReader(), dataSource));
    }

    private AudioTrack getM4ATrack(Object source) throws IOException, UnsupportedAudioFileException {
        final MP4Container cont;

        if (source instanceof RandomAccessFile)
            cont = new MP4Container((RandomAccessFile) source);
        else
            cont = new MP4Container((InputStream) source);

        final Movie movie = cont.getMovie();
        final List<net.sourceforge.jaad.mp4.api.Track> listContTracks =
                movie.getTracks(AudioTrack.AudioCodec.AAC);

        if (listContTracks.isEmpty())
            throw new UnsupportedAudioFileException("Movie does not contain any AAC track");

        return (AudioTrack) listContTracks.get(0);
    }

    private AudioInputStream decodeM4A(Object source) {
        try {
            final AudioTrack track;
            if (source instanceof File)
                track = getM4ATrack(new RandomAccessFile((File) source, "r"));
            else
                track = getM4ATrack(source);

            final Decoder dec = new Decoder(track.getDecoderSpecificInfo());
            final SampleBuffer buffer = new SampleBuffer();
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            final AudioFormat decFormat = new AudioFormat(track.getSampleRate(),
                    track.getSampleSize(), track.getChannelCount(),
                    true, true);

            Frame frame;
            while (track.hasMoreFrames()) {
                try {
                    frame = track.readNextFrame();
                    dec.decodeFrame(frame.getData(), buffer);
                    baos.write(buffer.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            final byte[] audioData = baos.toByteArray();
            return new AudioInputStream(new ByteArrayInputStream(audioData), decFormat, audioData.length);
        } catch (Exception e){
            Logger.getLogger(this, "Exception", e).error();
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected double convertSecondsToBytes(Number seconds) {
        final AudioFormat audioFormat = trackIO.getAudioFormat();
        final float frameRate = audioFormat.getFrameRate();
        final int frameSize = audioFormat.getFrameSize();
        final double framesToSeek = frameRate*seconds.doubleValue();
        return framesToSeek*frameSize;
    }

    @Override
    protected double convertBytesToSeconds(Number bytes) {
        final AudioFormat audioFormat = trackIO.getAudioFormat();
        return bytes.doubleValue() / audioFormat.getFrameSize() / audioFormat.getFrameRate();
    }

    @Override
    public void seek(double seconds) throws IOException {
        if (isAac) {
            mute();
            super.seek(seconds);
            unMute();
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
        else {
            pause();
            if (isAac) {
                resetStream();
            }
            else if (trackIO.resetDecodedStream()) {
                initSpeaker();
            }
            resumeTrack();
            trackData.setSecsSeeked(0);
            seek(second);
        }
    }

    /*private long getSoundSize() {
        long size = (long) ((decodedStream.getFormat().getSampleRate()/8)*getDuration());
        System.out.println("SoundSize: "+size);
        return size;
    }*/

}
