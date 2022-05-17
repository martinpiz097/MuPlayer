package org.muplayer.audio.format;

import org.jflac.sound.spi.FlacAudioFileReader;
import org.jflac.sound.spi.FlacFormatConversionProvider;
import org.muplayer.audio.Track;
import org.muplayer.info.TrackIO;
import org.muplayer.audio.codec.DecodeManager;
import org.muplayer.interfaces.Player;
import org.muplayer.util.AudioUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FlacTrack extends Track {

    public FlacTrack(File dataSource) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource);
    }

    public FlacTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath));
    }

    public FlacTrack(InputStream inputStream) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream);
    }

    public FlacTrack(File dataSource, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, player);
    }

    public FlacTrack(InputStream inputStream, Player player) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream, player);
    }

    public FlacTrack(String trackPath, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath, player);
    }

    @Override
    protected void loadAudioStream() {
        try {
            trackIO = new TrackIO();
            trackIO.setAudioReader(new FlacAudioFileReader());

            final AudioInputStream flacEncodedStream = AudioUtil.instanceStream(trackIO.getAudioReader(),
                    dataSource);
            final AudioFormat format = flacEncodedStream.getFormat();
            final AudioFormat decodedFormat = DecodeManager.getPcmFormatByFlac(format);
            trackIO.setDecodedStream(new FlacFormatConversionProvider().
                    getAudioInputStream(decodedFormat, flacEncodedStream));
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
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
    public synchronized void seek(double seconds) throws IOException {
        if (seconds == 0)
            return;
        trackData.setSecsSeeked(trackData.getSecsSeeked()+seconds);
        final int bytesToSeek = (int) Math.round(convertSecondsToBytes(seconds));

        AudioInputStream decodedStream = trackIO.getDecodedStream();
        if (decodedStream != null)
            decodedStream.read(new byte[bytesToSeek]);
    }

}
