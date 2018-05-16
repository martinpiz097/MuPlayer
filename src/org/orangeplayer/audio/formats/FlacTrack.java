package org.orangeplayer.audio.formats;

import org.kc7bfi.jflac.sound.spi.FlacAudioFileReader;
import org.kc7bfi.jflac.sound.spi.FlacFormatConversionProvider;
import org.orangeplayer.audio.Track;

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
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(),
                    format.getSampleSizeInBits(), format.getChannels(),
                    format.getChannels() * 2, format.getSampleRate(),
                    format.isBigEndian());

            speakerAis = new FlacFormatConversionProvider().
                    getAudioInputStream(decodedFormat, flacAis);


        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void seek(int seconds)
            throws IOException {
        long seek = transformSecondsInBytes(seconds);
        pause();
        speakerAis.read(new byte[(int) seek]);
        play();
        currentSeconds+=seconds;
    }

    @Override
    public void gotoSecond(int second) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        long bytes = transformSecondsInBytes(second);
        float currentVolume = trackLine.getControl(
                FloatControl.Type.MASTER_GAIN).getValue();
        if (bytes > ftrack.length())
            bytes = ftrack.length();

        pause();
        resetStream();
        trackLine.setGain(currentVolume);
        speakerAis.read(new byte[(int)bytes]);
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
