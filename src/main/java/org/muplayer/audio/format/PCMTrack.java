package org.muplayer.audio.format;

import com.sun.media.sound.AiffFileReader;
import com.sun.media.sound.AuFileReader;
import com.sun.media.sound.WaveFileReader;
import org.muplayer.audio.Track;
import org.muplayer.audio.interfaces.PlayerControls;
import org.muplayer.audio.util.AudioExtensions;
import org.muplayer.util.AudioUtil;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.muplayer.audio.util.AudioExtensions.*;

public class PCMTrack extends Track {

    public PCMTrack(File dataSource) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource);
    }

    public PCMTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath);
    }

    public PCMTrack(InputStream inputStream) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream);
    }

    public PCMTrack(File dataSource, PlayerControls player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, player);
    }

    public PCMTrack(InputStream inputStream, PlayerControls player) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream, player);
    }

    public PCMTrack(String trackPath, PlayerControls player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath, player);
    }

    /*@Override
    public long getDuration() {
        AudioFormat format = decodedStream.getFormat();
        // Bits por sample, channels y sampleRate
        System.out.println(format.getSampleSizeInBits());
        double bcm = format.getSampleSizeInBits()/8
                *format.getChannels()*format.getSampleRate();
        long fLen = dataSource.length();
        System.out.println("BCM: "+bcm);
        System.out.println("FL  en: "+fLen);
        return (long) (fLen / bcm);
    }

    @Override
    public String getDurationAsString() {
        long sec = getDuration();
        long min = sec / 60;
        sec = sec-(min*60);
        return new StringBuilder().append(min)
                .append(':').append(sec < 10 ? '0'+sec:sec).toString();
    }*/


    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        final String extension = AudioExtensions.getFormatName(dataSource.getName());
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
        trackStream = AudioUtil.instanceStream(audioReader, source);
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
}
