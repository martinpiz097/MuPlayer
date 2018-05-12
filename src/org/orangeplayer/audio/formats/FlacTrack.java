package org.orangeplayer.audio.formats;

import org.kc7bfi.jflac.sound.spi.FlacAudioFileReader;
import org.kc7bfi.jflac.sound.spi.FlacFormatConversionProvider;
import org.orangeplayer.audio.Track;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class FlacTrack extends Track {
    
    public FlacTrack(File ftrack) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(ftrack);
    }

    public FlacTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath);
    }

    @Override
    public boolean isValidTrack() {
        return true;
    }

    @Override
    protected void loadAudioStream() {
        /*try {
        audioReader = new FlacAudioFileReader();
        FlacDecoder decoder = new FlacDecoder(ftrack);
        if (decoder.isFlac()) {
            decoder.decode();
            speakerAis = decoder.getDecodedStream();
            decoder = null;
            System.out.println("FlacAis: "+speakerAis);
        }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
        try {
            audioReader = new FlacAudioFileReader();
            AudioInputStream flacAis = audioReader.getAudioInputStream(ftrack);

            AudioFormat format = flacAis.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(), format.getSampleSizeInBits(), format.getChannels(), format.getChannels() * 2,
                    format.getSampleRate(), format.isBigEndian());

            speakerAis = new FlacFormatConversionProvider().
                    getAudioInputStream(decodedFormat, flacAis);
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected short getSecondsByBytes(int readedBytes) {
        long secs = getDuration();
        long fLen = ftrack.length();
        return (short) ((readedBytes * secs) / fLen);
    }

    @Override
    public long getDuration() {
        AudioFormat format = speakerAis.getFormat();
        // Bits por sample, channels y sampleRate
        System.out.println(format.getSampleSizeInBits());
        double bcm = format.getSampleSizeInBits()
                *format.getChannels()*format.getSampleRate();
        long fLen = ftrack.length();

        System.out.println(format.getSampleRate());
        System.out.println(format.getFrameRate());
        System.out.println(format.getFrameSize());
        System.out.println(format.getSampleSizeInBits());
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
    }

    @Override
    public void seek(int seconds) {
        long secs = getDuration() / 1000 / 1000;
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

    public static void main(String[] args) {
        FlacTrack track = (FlacTrack) Track.getTrack(
                "/home/martin/AudioTesting/audio/flac2.flac");
        new Thread(track).start();
        track.setGain(80);
        System.out.println(track.getInfoSong());
    }


}
