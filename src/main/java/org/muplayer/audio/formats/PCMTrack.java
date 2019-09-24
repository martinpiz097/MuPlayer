package org.muplayer.audio.formats;

import com.sun.media.sound.AiffFileReader;
import com.sun.media.sound.AuFileReader;
import com.sun.media.sound.WaveFileReader;
import org.muplayer.audio.Track;
import org.muplayer.system.AudioUtil;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.muplayer.audio.util.AudioExtensions.*;

public class PCMTrack extends Track {

    public PCMTrack(File ftrack) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(ftrack);
    }

    public PCMTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath);
    }

    public PCMTrack(InputStream inputStream) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream);
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
        final AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(dataSource);
        final String extension = '.'+fileFormat.getType().getExtension().toLowerCase();
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

}
