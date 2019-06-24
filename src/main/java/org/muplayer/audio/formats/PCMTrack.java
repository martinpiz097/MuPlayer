package org.muplayer.audio.formats;

import com.sun.media.sound.AiffFileReader;
import com.sun.media.sound.AuFileReader;
import com.sun.media.sound.WaveFileReader;
import org.muplayer.audio.Track;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

import static org.muplayer.audio.util.AudioExtensions.*;

public class PCMTrack extends Track {

    public PCMTrack(File ftrack) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(ftrack);
    }

    public PCMTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath);
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
        System.out.println("FLen: "+fLen);
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
        trackStream = audioReader.getAudioInputStream(dataSource);
    }

    /*public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        Track track = new PCMTrack("/home/martin/AudioTesting/audio/wav2.wav");
        new Thread(track).start();
        System.out.println(track.getSongInfo());
        AudioInputStream ais = DigitalAudioSystem.getAudioInputStream(
                new File("/home/martin/AudioTesting/audio/sound.ogg"));
        Clip clip = DigitalAudioSystem.getClip();
        clip.open(ais);
        clip.start();
    }
    */

}
