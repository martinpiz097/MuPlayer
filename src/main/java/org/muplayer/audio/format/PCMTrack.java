package org.muplayer.audio.format;

import com.sun.media.sound.AiffFileReader;
import com.sun.media.sound.AuFileReader;
import com.sun.media.sound.WaveFileReader;
import org.muplayer.audio.Track;
import org.muplayer.info.TrackIO;
import org.muplayer.interfaces.PlayerControl;
import org.muplayer.util.AudioUtil;
import org.muplayer.util.FileUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class PCMTrack extends Track {

    private static final String WAVE = "wav";
    private static final String AIFF = "aiff";
    private static final String AIFC = "aifc";

    public PCMTrack(File dataSource) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource);
    }

    public PCMTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath);
    }

    public PCMTrack(InputStream inputStream) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream);
    }

    public PCMTrack(File dataSource, PlayerControl player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, player);
    }

    public PCMTrack(InputStream inputStream, PlayerControl player) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream, player);
    }

    public PCMTrack(String trackPath, PlayerControl player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath, player);
    }

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        trackIO = new TrackIO();
        final String extension = FileUtil.getFormatName(dataSource instanceof File
                ? ((File) dataSource).getName() : "");
        AudioFileReader audioReader;
        switch (extension) {
            case WAVE:
                audioReader = new WaveFileReader();
                break;
            case AIFF:
            case AIFC:
                audioReader = new AiffFileReader();
                break;
            default:
                audioReader = new AuFileReader();
                break;
        }
        AudioInputStream trackStream = AudioUtil.instanceStream(audioReader, dataSource);
        trackIO.setAudioReader(audioReader);
        trackIO.setDecodedStream(trackStream);
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
}
