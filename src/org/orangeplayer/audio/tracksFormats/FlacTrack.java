package org.orangeplayer.audio.tracksFormats;

import org.kc7bfi.jflac.sound.spi.FlacAudioFileReader;
import org.orangeplayer.audio.Track;
import org.orangeplayer.audio.codec.FlacDecoder;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
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
    protected void getAudioStream() {
        try {
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
        }
    }

    @Override
    public void seek(int seconds) {
        // Testing skip bytes
        try {
            speakerAis.skip(seconds);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FlacTrack track = (FlacTrack) Track.getTrack(
                "/home/martin/AudioTesting/audio/flac.flac");
        new Thread(track).start();
        System.out.println(track.getInfoSong());
    }


}
