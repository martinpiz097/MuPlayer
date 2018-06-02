package org.muplayer.audio.formats;

import org.kc7bfi.jflac.sound.spi.FlacAudioFileReader;
import org.kc7bfi.jflac.sound.spi.FlacFormatConversionProvider;
import org.muplayer.audio.Track;

import javax.sound.sampled.*;
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
        FlacDecoder decoder = new FlacDecoder(dataSource);
        if (decoder.isFlac()) {
            decoder.decode();
            trackStream = decoder.getDecodedStream();
            decoder = null;
            System.out.println("FlacAis: "+trackStream);
        }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
        try {
            audioReader = new FlacAudioFileReader();
            AudioInputStream flacAis = audioReader.getAudioInputStream(dataSource);

            AudioFormat format = flacAis.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(),
                    format.getSampleSizeInBits(), format.getChannels(),
                    format.getChannels() * 2, format.getSampleRate(),
                    format.isBigEndian());

            trackStream = new FlacFormatConversionProvider().
                    getAudioInputStream(decodedFormat, flacAis);


        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void seek(int seconds) throws IOException {
        long seek = transformSecondsInBytes(seconds);
        trackStream.read(new byte[(int) seek]);
        currentSeconds+=seconds;
    }

    @Override
    public void gotoSecond(int second) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        long bytes = transformSecondsInBytes(second);
        float currentVolume = trackLine.getControl(
                FloatControl.Type.MASTER_GAIN).getValue();
        if (bytes > dataSource.length())
            bytes = dataSource.length();

        pause();
        resetStream();
        trackLine.setGain(currentVolume);
        trackStream.read(new byte[(int)bytes]);
        play();
        currentSeconds = second;
    }



    /*public static void main(String[] args) {
        FlacTrack track = (FlacTrack) Track.getTrack(
                "/home/martin/AudioTesting/audio/flac.flac");
        new Thread(track).start();
        track.setGain(0);
        System.out.println(track.getInfoSong());
    }*/


}
