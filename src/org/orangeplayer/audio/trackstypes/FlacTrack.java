package org.orangeplayer.audio.trackstypes;

import org.kc7bfi.jflac.sound.spi.FlacAudioFileReader;
import org.orangeplayer.audio.Track;
import org.orangeplayer.audio.codec.FlacDecoder;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class FlacTrack extends Track {
    
    public FlacTrack(File ftrack)
            throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        super(ftrack);
    }

    public FlacTrack(String trackPath)
            throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(trackPath);
    }

    @Override
    protected void getAudioStream() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        audioReader = new FlacAudioFileReader();
        FlacDecoder decoder = new FlacDecoder();
        decoder.decode(ftrack);
        speakerAis = decoder.getDecodedStream();
        decoder = null;
        System.out.println("FlacAis: "+speakerAis);
    }

    @Override
    public void seek(int seconds) throws Exception {
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
    }


}
