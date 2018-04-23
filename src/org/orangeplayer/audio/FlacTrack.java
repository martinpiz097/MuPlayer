package org.orangeplayer.audio;

import org.orangeplayer.audio.codec.FlacDecoder;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class FlacTrack extends Track {
    
    protected FlacTrack(File ftrack)
            throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        super(ftrack);
    }

    protected FlacTrack(String trackPath)
            throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(trackPath);
    }

    @Override
    protected void getAudioStream() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
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

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        FlacTrack track = (FlacTrack) Track.getTrack(
                "/home/martin/AudioTesting/audio/flac.flac");
        new Thread(track).start();
    }


}
