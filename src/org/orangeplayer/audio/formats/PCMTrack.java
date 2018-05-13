package org.orangeplayer.audio.formats;

import com.sun.media.sound.AiffFileReader;
import com.sun.media.sound.AuFileReader;
import com.sun.media.sound.WaveFileReader;
import org.orangeplayer.audio.Track;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

import static org.orangeplayer.audio.AudioExtensions.*;

public class PCMTrack extends Track {

    public PCMTrack(File ftrack) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(ftrack);
    }

    public PCMTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath);
    }

    @Override
    protected short getSecondsByBytes(int readedBytes) {
        long secs = getDuration();
        long fLen = ftrack.length();
        return (short) ((readedBytes * secs) / fLen);
    }

    /*@Override
    public long getDuration() {
        AudioFormat format = speakerAis.getFormat();
        // Bits por sample, channels y sampleRate
        System.out.println(format.getSampleSizeInBits());
        double bcm = format.getSampleSizeInBits()/8
                *format.getChannels()*format.getSampleRate();
        long fLen = ftrack.length();
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
        final AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(ftrack);
        final String extension = fileFormat.getType().getExtension();
        switch (extension) {
            case WAVE:
                audioReader = new WaveFileReader();
                break;
            case AIFF:
                audioReader = new AiffFileReader();
                break;
            case AIFC:
                audioReader = new AiffFileReader();
                break;
            default:
                audioReader = new AuFileReader();
                break;
        }
        speakerAis = audioReader.getAudioInputStream(ftrack);
    }

    @Override
    public void seek(int seconds) {
        long secs = getDuration();
        long fLen = ftrack.length();
        long seekLen = (seconds * fLen) / secs;
        try {
            if (seekLen > speakerAis.available())
                seekLen = speakerAis.available();
            speakerAis.skip(seekLen);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        Track track = new PCMTrack("/home/martin/AudioTesting/audio/wav2.wav");
        new Thread(track).start();
        System.out.println(track.getInfoSong());
        /*AudioInputStream ais = AudioSystem.getAudioInputStream(
                new File("/home/martin/AudioTesting/audio/sound.ogg"));
        Clip clip = AudioSystem.getClip();
        clip.open(ais);
        clip.start();*/
    }

}
